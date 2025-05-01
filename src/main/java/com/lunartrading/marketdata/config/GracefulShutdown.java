package com.lunartrading.marketdata.config;

import java.util.concurrent.CompletableFuture;

public interface GracefulShutdown {
    CompletableFuture<Void> shutdown();
}
