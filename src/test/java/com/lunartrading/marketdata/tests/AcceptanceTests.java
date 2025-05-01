package com.lunartrading.marketdata.tests;

import com.lunartrading.marketdata.testsupport.LunarTestingBase;
import com.lunartrading.marketdata.testsupport.TestingLevel;
import org.junit.jupiter.api.Test;

import static com.lunartrading.DomainObjectFactory.*;

public class AcceptanceTests extends LunarTestingBase {

    protected AcceptanceTests() {
        super(TestingLevel.ACCEPTANCE);
    }

    @Test
    public void shouldProcessInitialMessage() {

        Given.exchange().publishesPartial(
                bids -> bids
                        .level(PRICE200_0, 1000)
                        .level(PRICE199_5, 900)
                        .level(PRICE199_0, 800)
                        .level(PRICE198_5, 700)
                ,
                asks -> asks
                        .level(PRICE200_5, 600)
                        .level(PRICE201_0, 500)
                        .level(PRICE201_5, 400)
                        .level(PRICE202_0, 300)

        );
        Expect.latestSnapshot().hasBook(
                bids -> {
                    bids.level(PRICE200_0, 1000);
                    bids.level(PRICE199_5, 900);
                    bids.level(PRICE199_0, 800);
                },
                asks -> {
                    asks.level(PRICE200_5, 600);
                    asks.level(PRICE201_0, 500);
                    asks.level(PRICE201_5, 400);
                }
        );
    }

    @Test
    public void shouldReplaceWholeBookOnPartialMessage() {

        Given.exchange().publishesPartial(
                bids -> {
                    bids.level(PRICE200_0, 1000);
                    bids.level(PRICE199_5, 900);
                    bids.level(PRICE199_0, 800);
                    bids.level(PRICE198_5, 700);
                },
                asks -> {
                    asks.level(PRICE200_5, 600);
                    asks.level(PRICE201_0, 500);
                    asks.level(PRICE201_5, 400);
                    asks.level(PRICE202_0, 300);
                }
        );

        Given.exchange().publishesPartial(
                bids -> bids
                        .level(PRICE200_0, 1001)
                        .level(PRICE199_0, 900),
                asks -> asks
                        .level(PRICE201_0, 5001)
                        .level(PRICE202_0, 1013));

        Expect.latestSnapshot().hasBook(
                bids -> {
                    bids.level(PRICE200_0, 1001);
                    bids.level(PRICE199_0, 900);
                },
                asks -> {
                    asks.level(PRICE201_0, 5001);
                    asks.level(PRICE202_0, 1013);
                }
        );
    }

    @Test
    public void shouldNotPublishSnapshotOutsideN() throws InterruptedException {

        Given.exchange().publishesPartial(
                bids -> {
                    bids.level(PRICE200_0, 1000);
                    bids.level(PRICE199_5, 900);
                    bids.level(PRICE199_0, 800);
                    bids.level(PRICE198_5, 700);
                },
                asks -> {
                    asks.level(PRICE200_5, 600);
                    asks.level(PRICE201_0, 500);
                    asks.level(PRICE201_5, 400);
                    asks.level(PRICE202_0, 300);
                }
        );

        Expect.latestSnapshot().hasSnapshotCount(1);

        Given.exchange().publishesUpdate(
                bids -> bids
                        .level(PRICE198_5, 900),
                asks -> asks
                        .level(PRICE202_0, 5001));

        Expect.latestSnapshot().hasSnapshotCount(1);
    }

    @Test
    public void shouldPublishSnapshotInsideN() throws InterruptedException {

        Given.exchange().publishesPartial(
                bids -> {
                    bids.level(PRICE200_0, 1000);
                    bids.level(PRICE199_5, 900);
                    bids.level(PRICE199_0, 800);
                    bids.level(PRICE198_5, 700);
                },
                asks -> {
                    asks.level(PRICE200_5, 600);
                    asks.level(PRICE201_0, 500);
                    asks.level(PRICE201_5, 400);
                    asks.level(PRICE202_0, 300);
                }
        );

        Expect.latestSnapshot().hasSnapshotCount(1);

        Given.exchange().publishesUpdate(
                bids -> bids
                        .level(PRICE199_0, 900),
                asks -> asks
                        .level(PRICE201_5, 5001));

        Expect.latestSnapshot().hasSnapshotCount(2);
    }

    @Test
    public void shouldPublishSnapshotOnDeleteFollowedByUpdate() throws InterruptedException {

        Given.exchange().publishesPartial(
                bids -> {
                    bids.level(PRICE200_0, 1000);
                    bids.level(PRICE199_5, 900);
                    bids.level(PRICE199_0, 800);
                    bids.level(PRICE198_5, 700);
                },
                asks -> {
                    asks.level(PRICE200_5, 600);
                    asks.level(PRICE201_0, 500);
                    asks.level(PRICE201_5, 400);
                    asks.level(PRICE202_0, 300);
                }
        );

        Expect.latestSnapshot().hasSnapshotCount(1);

        Given.exchange().publishesDelete(
                bids -> bids
                        .level(PRICE198_5),
                asks -> asks
                        .level(PRICE202_0));

        Expect.latestSnapshot().hasSnapshotCount(1);

        Given.exchange().publishesUpdate(
                bids -> bids
                        .level(PRICE198_5, 901),
                asks -> {
                });


        Expect.latestSnapshot().hasSnapshotCount(2);
    }


    @Test
    public void shouldRemoveBookLevelsOnDeleteMessage() {

        Given.exchange().publishesPartial(
                bids -> bids
                        .level(PRICE200_0, 1000)
                        .level(PRICE199_0, 2000),
                asks -> asks
                        .level(PRICE201_0, 500)
                        .level(PRICE202_0, 1000));

        Given.exchange().publishesDelete(
                bids -> bids
                        .level(PRICE200_0),
                asks -> asks
                        .level(PRICE202_0));

        Expect.latestSnapshot().hasBook(
                bids -> bids
                        .level(PRICE199_0, 2000),
                asks -> asks
                        .level(PRICE201_0, 500));
    }

    @Test
    public void shouldUpdateBookLevelsOnUpdateMessage() {

        Given.exchange().publishesPartial(
                bids -> bids
                        .level(PRICE200_0, 1000)
                        .level(PRICE199_0, 2000),
                asks -> asks
                        .level(PRICE201_0, 500)
                        .level(PRICE202_0, 1000));

        Given.exchange().publishesUpdate(
                bids -> bids
                        .level(PRICE200_0, 1013),
                asks -> asks
                        .level(PRICE202_0, 2013));

        Expect.latestSnapshot().hasBook(
                bids -> bids
                        .level(PRICE200_0, 1013)
                        .level(PRICE199_0, 2000),
                asks -> asks
                        .level(PRICE201_0, 500)
                        .level(PRICE202_0, 2013));
    }

    @Test
    public void shouldUpdateBookLevelsOnInsertMessage() {

        Given.exchange().publishesPartial(
                bids -> bids
                        .level(PRICE199_0, 2000),
                asks -> asks
                        .level(PRICE202_0, 1000));

        Given.exchange().publishesInsert(
                bids -> bids
                        .level(PRICE200_0, 1014),
                asks -> asks
                        .level(PRICE201_0, 2013));

        Expect.latestSnapshot().hasBook(
                bids -> bids
                        .level(PRICE200_0, 1014)
                        .level(PRICE199_0, 2000),
                asks -> asks
                        .level(PRICE201_0, 2013)
                        .level(PRICE202_0, 1000));
    }

    @Test
    public void shouldTrackLatestTimestampCorrectly() {

        Given.exchange().publishesPartial(
                bids -> bids
                        .level(PRICE199_0, 1000, TIMESTAMP_1),
                asks -> asks
                        .level(PRICE200_5, 1000, TIMESTAMP_1)
        );

        Given.exchange().publishesUpdate(
                bids -> bids
                        .level(PRICE199_0, 2000, TIMESTAMP_2),
                asks -> {
                }
        );

        Expect.latestSnapshot().hasLatestTimestamp(TIMESTAMP_2);
    }

    @Test
    public void shouldNotMixSymbolsInSameBook() {

        Given.exchange().publishesPartial(
                bids -> bids
                        .level(PRICE199_0, 500),
                asks -> asks
                        .level(PRICE200_5, 500)
        );

        Given.exchange().publishesPartial(
                bids -> bids
                        .withSymbol(ANOTHER_SYMBOL)
                        .level(PRICE198_5, 800),
                asks -> asks
                        .withSymbol(ANOTHER_SYMBOL)
                        .level(PRICE201_5, 900)
        );

        Expect.latestSnapshot().hasBook(
                bids -> bids.level(PRICE199_0, 500),
                asks -> asks.level(PRICE200_5, 500)
        );

        Expect.latestSnapshot(ANOTHER_SYMBOL).hasBook(
                bids -> bids
                        .withSymbol(ANOTHER_SYMBOL)
                        .level(PRICE198_5, 800),
                asks -> asks
                        .withSymbol(ANOTHER_SYMBOL)
                        .level(PRICE201_5, 900)
        );
    }

    @Test
    public void shouldPublishSnapshotOnEveryUpdateWithinTopN() {
        Given.exchange().publishesPartial(
                bids -> bids.level(PRICE200_0, 1000),
                asks -> asks.level(PRICE200_5, 500)
        );

        Given.exchange().publishesInsert(
                bids -> bids.level(PRICE199_5, 1100),  // New best bid
                asks -> {
                }
        );

        Expect.latestSnapshot().hasBook(
                bids -> {
                    bids.level(PRICE199_5, 1100);
                    bids.level(PRICE200_0, 1000);
                },
                asks -> asks.level(PRICE200_5, 500)
        );
    }

}
