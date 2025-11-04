package com.udacity.image.service;

/**
 * Parameters for image processing operations.
 */
public class ImageProcessingParameters {
    private final int width;
    private final int height;
    private final String format;
    private final float quality;

    private ImageProcessingParameters(Builder builder) {
        this.width = builder.width;
        this.height = builder.height;
        this.format = builder.format;
        this.quality = builder.quality;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getFormat() {
        return format;
    }

    public float getQuality() {
        return quality;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int width = -1;
        private int height = -1;
        private String format = "jpg";
        private float quality = 0.9f;

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setFormat(String format) {
            this.format = format;
            return this;
        }

        public Builder setQuality(float quality) {
            this.quality = quality;
            return this;
        }

        public ImageProcessingParameters build() {
            return new ImageProcessingParameters(this);
        }
    }
}