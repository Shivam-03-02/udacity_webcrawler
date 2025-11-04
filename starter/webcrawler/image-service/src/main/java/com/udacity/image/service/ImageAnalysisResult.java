package com.udacity.image.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Results from analyzing an image.
 */
public class ImageAnalysisResult {
    private final Map<String, Float> detectedObjects;
    private final Map<String, String> metadata;
    private final boolean hasExifData;
    private final int width;
    private final int height;

    private ImageAnalysisResult(Builder builder) {
        this.detectedObjects = Collections.unmodifiableMap(new HashMap<>(builder.detectedObjects));
        this.metadata = Collections.unmodifiableMap(new HashMap<>(builder.metadata));
        this.hasExifData = builder.hasExifData;
        this.width = builder.width;
        this.height = builder.height;
    }

    public Map<String, Float> getDetectedObjects() {
        return detectedObjects;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public boolean hasExifData() {
        return hasExifData;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Map<String, Float> detectedObjects = new HashMap<>();
        private Map<String, String> metadata = new HashMap<>();
        private boolean hasExifData;
        private int width;
        private int height;

        public Builder setDetectedObjects(Map<String, Float> detectedObjects) {
            this.detectedObjects = new HashMap<>(detectedObjects);
            return this;
        }

        public Builder setMetadata(Map<String, String> metadata) {
            this.metadata = new HashMap<>(metadata);
            return this;
        }

        public Builder setHasExifData(boolean hasExifData) {
            this.hasExifData = hasExifData;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public ImageAnalysisResult build() {
            return new ImageAnalysisResult(this);
        }
    }
}