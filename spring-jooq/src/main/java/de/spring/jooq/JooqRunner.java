// Created: 25.02.2026
package de.spring.jooq;

import java.math.BigDecimal;
import java.util.Objects;

import de.spring.jooq.model.Sequences;
import de.spring.jooq.model.Tables;
import de.spring.jooq.model.tables.records.CustomerRecord;
import org.jooq.DSLContext;
import org.jooq.ExecuteType;
import org.jooq.Result;
import org.jooq.SelectWhereStep;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
@Order(2)
// @Profile("!test")
public class JooqRunner implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(JooqRunner.class);

    private final DSLContext dslContext;

    public JooqRunner(final DSLContext dslContext) {
        super();

        this.dslContext = Objects.requireNonNull(dslContext, "dslContext required");
    }

    @Override
    public void run(final String... args) {
        LOGGER.info("Running JooqRunner...");

        try {
            insert();
            queries();
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private void insert() {
        // final Sequence<BigInteger> sequence = DSL.sequence(DSL.name("CUSTOMER_SEQ"));
        // sequence.nextval();

        for (int i = 1; i <= 1; i++) {
            dslContext.insertInto(Tables.CUSTOMER,
                            Tables.CUSTOMER.ID,
                            Tables.CUSTOMER.CUSTOMER_NO,
                            Tables.CUSTOMER.NAME,
                            Tables.CUSTOMER.EMAIL)
                    .values(Sequences.CUSTOMER_SEQ.nextval(),
                            DSL.value("NO-" + i),
                            DSL.value("NAME-\"'\"-" + i),
                            DSL.value("MAIL-" + i))
                    .execute();

            dslContext.insertInto(Tables.ORDERS,
                            Tables.ORDERS.ID,
                            Tables.ORDERS.CUSTOMER_ID,
                            Tables.ORDERS.ORDER_NO)
                    .values(Sequences.ORDERS_SEQ.nextval(),
                            Sequences.CUSTOMER_SEQ.currval(),
                            DSL.value("ORDER_NO-1"))
                    .execute();

            dslContext.insertInto(Tables.ORDER_ITEM,
                            Tables.ORDER_ITEM.ID,
                            Tables.ORDER_ITEM.ORDER_ID,
                            Tables.ORDER_ITEM.POSITION_NO,
                            Tables.ORDER_ITEM.PRODUCT_CODE,
                            Tables.ORDER_ITEM.QUANTITY,
                            Tables.ORDER_ITEM.UNIT_PRICE
                    )
                    .values(Sequences.ORDER_ITEM_SEQ.nextval(),
                            Sequences.ORDERS_SEQ.currval(),
                            DSL.value(1),
                            DSL.value("PRODUCT_CODE-1"),
                            DSL.value(1),
                            DSL.value(BigDecimal.valueOf(1.99D))
                    )
                    .execute();
        }
    }

    private void queries() {
        final SelectWhereStep<CustomerRecord> selectWhereStep = dslContext.selectFrom(Tables.CUSTOMER);
        LOGGER.info("SQL: {}", selectWhereStep.getSQL());

        final Result<CustomerRecord> result = selectWhereStep.fetch();
        LOGGER.info("{}", result.formatCSV());
        // result.formatCSV(System.out, new CSVFormat().quote(CSVFormat.Quote.ALWAYS));

        result.forEach(rec -> LOGGER.info("CustomerRecord: {}", rec));

        dslContext.select(Tables.CUSTOMER.NAME, Tables.ORDERS.ORDER_DATE)
                .from(Tables.CUSTOMER)
                .join(Tables.ORDERS).on(Tables.ORDERS.CUSTOMER_ID.eq(Tables.CUSTOMER.ID))
                .orderBy(Tables.CUSTOMER.NAME.asc().nullsFirst(), Tables.ORDERS.ORDER_DATE.desc().nullsFirst())
                .fetch()
                .forEach(r -> LOGGER.info("{} | {}", r.get(Tables.CUSTOMER.NAME), r.get(Tables.ORDERS.ORDER_DATE)));

        LOGGER.info("----------");
        LOGGER.info("STATISTICS");
        LOGGER.info("----------");

        for (ExecuteType executeType : ExecuteType.values()) {
            final String statistic = String.format("%-8s: %d executions", executeType.name(), StatisticsListener.getOrDefault(executeType, 0));
            LOGGER.info(statistic);
        }

        // Should raise DeleteOrUpdateWithoutWhereListener.DeleteOrUpdateWithoutWhereException.
        dslContext.deleteFrom(Tables.ORDER_ITEM).execute();
    }
}
