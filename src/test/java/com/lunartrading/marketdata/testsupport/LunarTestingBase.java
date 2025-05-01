package com.lunartrading.marketdata.testsupport;

import com.lunartrading.marketdata.domain.BitMexMessage;
import com.lunartrading.marketdata.orderbook.OrderBookSnapshot;
import com.lunartrading.marketdata.service.OrderBookService;
import io.reactivex.rxjava3.processors.PublishProcessor;
import org.junit.jupiter.api.AfterEach;

public abstract class LunarTestingBase {

    //wire these by hand as opposed to springboot magic for now and for clarity and simplicity.
    private final int DEFAULT_SNAPSHOT_LEVEL_SIZE = 3;

    protected final PublishProcessor<BitMexMessage> sockerProcessor = PublishProcessor.create();

    protected final Given Given;

    protected final PublishProcessor<OrderBookSnapshot> snapshotProcessor = PublishProcessor.create();
    protected final Expect Expect;

    @SuppressWarnings("unused")
    private OrderBookService orderBookService = new OrderBookService(
            sockerProcessor, snapshotProcessor, DEFAULT_SNAPSHOT_LEVEL_SIZE);

    protected LunarTestingBase(TestingLevel level) {
        Given = new Given(sockerProcessor, level);
        Expect = new Expect(snapshotProcessor, level);
    }

    @AfterEach
    void afterEach() {
        Given.clear();
        Expect.clear();
        orderBookService = new OrderBookService(
                sockerProcessor, snapshotProcessor, DEFAULT_SNAPSHOT_LEVEL_SIZE);
    }

}
