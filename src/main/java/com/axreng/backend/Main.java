package com.axreng.backend;

import com.axreng.backend.config.AppConfig;

public class Main {
    public static void main(String[] args) {
        new AppConfig().configure();
    }
}
