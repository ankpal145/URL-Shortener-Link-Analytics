package com.urlshortener.config;

import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Static-asset caching for the Vite-built SPA that ships inside the jar.
 *
 * <p>Hashed files under {@code /assets/**} are content-addressed and can be
 * cached forever. {@code index.html} must never be cached so clients always
 * pick up a new deploy's asset hashes.</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable());

        registry.addResourceHandler("/index.html")
                .addResourceLocations("classpath:/static/index.html")
                .setCacheControl(CacheControl.noCache().mustRevalidate());
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Explicit welcome mapping so "/" always serves the SPA entrypoint.
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}
