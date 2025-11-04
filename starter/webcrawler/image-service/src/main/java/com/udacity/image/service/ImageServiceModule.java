package com.udacity.image.service;

import com.google.inject.AbstractModule;

/**
 * Guice module for binding the ImageService implementation.
 */
public class ImageServiceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ImageService.class).to(ImageServiceImpl.class);
    }
}