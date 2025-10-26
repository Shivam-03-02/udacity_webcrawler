package com.udacity.webcrawler;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

final class ParallelWebCrawler implements WebCrawler {
  private final Clock clock;
  private final Duration timeout;
  private final int popularWordCount;
  private final PageParserFactory parserFactory;
  private final int maxDepth;
  private final List<Pattern> ignoredUrls;
  private final ForkJoinPool pool;

  @Inject
  ParallelWebCrawler(
      Clock clock,
      PageParserFactory parserFactory,
      @Timeout Duration timeout,
      @PopularWordCount int popularWordCount,
      @MaxDepth int maxDepth,
      @IgnoredUrls List<Pattern> ignoredUrls,
      @TargetParallelism int threadCount) {
    this.clock = clock;
    this.parserFactory = parserFactory;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.maxDepth = maxDepth;
    this.ignoredUrls = ignoredUrls;
    this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
  }

  @Override
  public CrawlResult crawl(List<String> startingUrls) {
    Instant deadline = clock.instant().plus(timeout);

    Map<String, Integer> counts = new ConcurrentHashMap<>();
    Set<String> visitedUrls = new ConcurrentSkipListSet<>();

    class CrawlerTask extends ForkJoinTask<Void> {
      private final String url;
      private final int depth;

      CrawlerTask(String url, int depth) {
        this.url = url;
        this.depth = depth;
      }

      @Override
      public Void getRawResult() {
        return null;
      }

      @Override
      protected void setRawResult(Void value) {
      }

      @Override
      public boolean exec() {
        crawl(url, depth);
        return true;
      }

      private void crawl(String url, int depth) {
        if (depth == 0 || clock.instant().isAfter(deadline)) {
          return;
        }
        for (Pattern pattern : ignoredUrls) {
          if (pattern.matcher(url).matches()) {
            return;
          }
        }
        if (!visitedUrls.add(url)) {
          return;
        }

        PageParser.Result result = parserFactory.get(url).parse();

        result.getWordCounts().forEach((word, count) ->
            counts.merge(word, count, Integer::sum)
        );

        List<CrawlerTask> subtasks = result.getLinks().stream()
            .map(link -> new CrawlerTask(link, depth - 1))
            .collect(Collectors.toList());
        if (!subtasks.isEmpty()) {
          ForkJoinTask.invokeAll(subtasks);
        }
      }
    }

    List<ForkJoinTask<?>> tasks = startingUrls.stream()
        .map(url -> pool.submit(new CrawlerTask(url, maxDepth)))
        .collect(Collectors.toList());

    for (ForkJoinTask<?> t : tasks) {
      t.join();
    }

    if (counts.isEmpty()) {
      return new CrawlResult.Builder()
          .setWordCounts(counts)
          .setUrlsVisited(visitedUrls.size())
          .build();
    }

    return new CrawlResult.Builder()
        .setWordCounts(WordCounts.sort(counts, popularWordCount))
        .setUrlsVisited(visitedUrls.size())
        .build();
  }

  @Override
  public int getMaxParallelism() {
    return Runtime.getRuntime().availableProcessors();
  }
}
