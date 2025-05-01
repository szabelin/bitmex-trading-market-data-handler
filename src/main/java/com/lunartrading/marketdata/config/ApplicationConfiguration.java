package com.lunartrading.marketdata.config;

import com.lunartrading.marketdata.domain.BitMexMessage;
import com.lunartrading.marketdata.orderbook.OrderBookSnapshot;
import io.reactivex.rxjava3.processors.PublishProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public PublishProcessor<BitMexMessage> messageProcessor() {
        return PublishProcessor.create();
    }

    @Bean
    public PublishProcessor<OrderBookSnapshot> snapshotProcessor() {
        return PublishProcessor.create();
    }

}
