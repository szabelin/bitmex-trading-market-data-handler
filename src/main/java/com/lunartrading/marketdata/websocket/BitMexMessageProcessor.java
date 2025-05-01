package com.lunartrading.marketdata.websocket;

import com.lunartrading.marketdata.domain.BitMexMessage;
import com.lunartrading.marketdata.serialization.FastBitMexMessageParser;
import io.reactivex.rxjava3.processors.PublishProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/***
 * Parses raw JSON messages and publishes them to RxJava subscribers
 */
@Component
public class BitMexMessageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(BitMexMessageProcessor.class);
    private final PublishProcessor<BitMexMessage> messageProcessor;

    @Autowired
    public BitMexMessageProcessor(PublishProcessor<BitMexMessage> messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    public void process(String text) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Received message: {}", text);
            }

            BitMexMessage bitMexMessage = FastBitMexMessageParser.parse(text);

            if (!BitMexMessage.INVALID.equals(bitMexMessage)) {
                messageProcessor.onNext(bitMexMessage);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Processed message: table={}, action={}, entries={}",
                        bitMexMessage.table(), bitMexMessage.action(),
                        bitMexMessage.data().size());
            }


        } catch (Exception e) {
            logger.error("Error processing message: {}", e.getMessage(), e);
        }
    }
}
