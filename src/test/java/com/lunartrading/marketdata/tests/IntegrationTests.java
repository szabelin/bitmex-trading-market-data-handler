package com.lunartrading.marketdata.tests;

import com.lunartrading.marketdata.testsupport.LunarTestingBase;
import com.lunartrading.marketdata.testsupport.TestingLevel;
import org.junit.jupiter.api.Test;

import static com.lunartrading.DomainObjectFactory.PRICE200_0;
import static com.lunartrading.DomainObjectFactory.PRICE200_5;

/***
 * Showing what can be done on the end to end level
 * Can be same as Acceptance Tests
 * (Best to configure profiles in TeamCity)
 */
public class IntegrationTests extends LunarTestingBase {

    protected IntegrationTests() {
        super(TestingLevel.INTEGRATION);
    }

    @Test
    public void demoEndToEndTest() {
        Given.exchange().publishesInsert(
                bids -> bids
                        .level(PRICE200_0, 1000)
                ,
                asks -> asks
                        .level(PRICE200_5, 600)
        );

        Expect.latestSnapshot().hasBook(
                bids -> bids
                        .level(PRICE200_0, 1000)
                ,
                asks -> asks
                        .level(PRICE200_5, 600)
        );

    }

}
