package com.udacity.image.service;

/**
 * Interface for Image Service operations.
 * This service provides image analysis and processing capabilities that can be used
 * by multiple applications.
 */
public interface ImageService {
    /**
     * Analyzes an image to determine its content and metadata.
     *
     * @param imageData The raw image data as a byte array
     * @return An ImageAnalysisResult containing the analysis results
     */
    ImageAnalysisResult analyzeImage(byte[] imageData);

    /**
     * Processes an image according to specified parameters.
     *
     * @param imageData The raw image data to process
     * @param params Processing parameters
     * @return The processed image data
     */
    byte[] processImage(byte[] imageData, ImageProcessingParameters params);
}