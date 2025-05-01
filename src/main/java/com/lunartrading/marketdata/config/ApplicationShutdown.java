package com.lunartrading.marketdata.config;

import io.reactivex.rxjava3.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ApplicationShutdown {
    private static final Logger log = LoggerFactory.getLogger(ApplicationShutdown.class);
    private final List<GracefulShutdown> services;

    public ApplicationShutdown(GracefulShutdown... services) {
        this.services = Arrays.stream(services).toList();
    }

    public static CompletableFuture<Void> closeRxSubscription(Disposable disposable) {
        return CompletableFuture.runAsync(disposable::dispose);
    }

    @EventListener(ContextClosedEvent.class)
    public void onApplicationEnd() {
        services.forEach(service -> {
            try {
                service.shutdown().join();
            } catch (Exception e) {
                log.warn("Error encountered during service shutdown", e);
            }
        });
    }
}
