package com.lunartrading.marketdata.testsupport.websocket;

import com.lunartrading.marketdata.testsupport.SideBuilder;

import java.util.function.Consumer;

public interface ExchangeSimulator {

    void publishesPartial(Consumer<SideBuilder> bidBuilder,
                          Consumer<SideBuilder> askBuilder);

    void publishesDelete(Consumer<SideBuilder> bidBuilder,
                         Consumer<SideBuilder> askBuilder);

    void publishesUpdate(Consumer<SideBuilder> bidBuilder,
                         Consumer<SideBuilder> askBuilder);

    void publishesInsert(Consumer<SideBuilder> bidBuilder,
                         Consumer<SideBuilder> askBuilder);
}
