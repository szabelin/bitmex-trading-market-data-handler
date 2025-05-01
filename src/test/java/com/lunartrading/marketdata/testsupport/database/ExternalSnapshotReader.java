package com.lunartrading.marketdata.testsupport.database;

import com.lunartrading.marketdata.orderbook.OrderBookSnapshot;
import com.lunartrading.marketdata.service.OrderBookSnapshotPersistenceService;
import io.reactivex.rxjava3.processors.PublishProcessor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.util.List;

public class ExternalSnapshotReader implements SnapshotSource {

    private final OrderBookSnapshotReaderForTests reader;

    public ExternalSnapshotReader(PublishProcessor<OrderBookSnapshot> snapshotProcessor) {
        JdbcTemplate jdbcTemplate = createInMemoryH2Template();

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource("schema.sql")
        );
        populator.execute(jdbcTemplate.getDataSource());

        new OrderBookSnapshotPersistenceService(snapshotProcessor, jdbcTemplate);
        this.reader = new OrderBookSnapshotReaderForTests(jdbcTemplate);
    }

    @Override
    public List<OrderBookSnapshot> getSnapshots(String symbol) {
        return reader.readAllSnapshots().stream()
                .filter(s -> s.symbol().equals(symbol))
                .toList();
    }

    @Override
    public void close() {

    }

    private static JdbcTemplate createInMemoryH2Template() {
        DataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                "sa",
                ""
        );
        return new JdbcTemplate(dataSource);
    }
}
