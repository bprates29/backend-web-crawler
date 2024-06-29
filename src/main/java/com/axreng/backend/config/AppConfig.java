package com.axreng.backend.config;

import com.axreng.backend.controller.CrawlController;

public class AppConfig {
    public void configure() {
        new CrawlController().registerRoutes();
    }
}
