package com.lunartrading.marketdata.testsupport;

import com.lunartrading.marketdata.orderbook.OrderBookSnapshot;
import com.lunartrading.marketdata.testsupport.database.ExternalSnapshotReader;
import com.lunartrading.marketdata.testsupport.database.InternalSnapshotReader;
import com.lunartrading.marketdata.testsupport.database.SnapshotSource;
import com.lunartrading.marketdata.testsupport.database.SnapshotsCollector;
import io.reactivex.rxjava3.processors.PublishProcessor;

import static com.lunartrading.DomainObjectFactory.DEFAULT_SYMBOL;

public class Expect {

    private final SnapshotSource snapshotSource;

    public Expect(PublishProcessor<OrderBookSnapshot> snapshotProcessor, TestingLevel level) {
        this.snapshotSource = (level == TestingLevel.ACCEPTANCE)
                ? new InternalSnapshotReader(snapshotProcessor)
                : new ExternalSnapshotReader(snapshotProcessor);
    }

    public SnapshotsCollector latestSnapshot() {
        return latestSnapshot(DEFAULT_SYMBOL);
    }

    public SnapshotsCollector latestSnapshot(String symbol) {
        return new SnapshotsCollector(() -> snapshotSource.getSnapshots(symbol), symbol);
    }

    public void clear() {
        snapshotSource.close();
    }

}
