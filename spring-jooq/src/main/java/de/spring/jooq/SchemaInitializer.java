// Created: 30.06.2026
package de.spring.jooq;

import static org.jooq.impl.DSL.constraint;
import static org.jooq.impl.DSL.currentLocalDate;
import static org.jooq.impl.DSL.currentTimestamp;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.sequence;
import static org.jooq.impl.DSL.table;
import static org.jooq.impl.SQLDataType.BIGINT;
import static org.jooq.impl.SQLDataType.DECIMAL;
import static org.jooq.impl.SQLDataType.INTEGER;
import static org.jooq.impl.SQLDataType.LOCALDATE;
import static org.jooq.impl.SQLDataType.TIMESTAMP;
import static org.jooq.impl.SQLDataType.VARCHAR;

import java.math.BigDecimal;

import org.jooq.DSLContext;

/**
 * @author Thomas Freese
 */
public final class SchemaInitializer {

    public void initialize(final DSLContext ctx) {

        // 1) Schema
        ctx.createSchemaIfNotExists("APP").execute();

        // 2) Sequences (vor den Tabellen, falls als DEFAULT genutzt).
        ctx.createSequenceIfNotExists(name("APP", "CUSTOMER_SEQ"))
                .startWith(1L)
                .incrementBy(1L)
                .minvalue(1L)
                .noMaxvalue()
                .cache(20)
                .noCycle()
                .execute();

        ctx.createSequenceIfNotExists(name("APP", "ORDERS_SEQ"))
                .startWith(1L)
                .incrementBy(1L)
                .cache(50)
                .noCycle()
                .execute();

        ctx.createSequenceIfNotExists(name("APP", "ORDER_ITEM_SEQ"))
                .startWith(1L)
                .incrementBy(1L)
                .cache(50)
                .noCycle()
                .execute();

        // 3) Tabelle CUSTOMER – ID per Sequence-Default.
        ctx.createTableIfNotExists(name("APP", "CUSTOMER"))
                .column("ID", BIGINT.nullable(false).defaultValue(sequence(name("APP", "CUSTOMER_SEQ"), BIGINT).nextval()))
                .column("CUSTOMER_NO", VARCHAR(20).nullable(false))
                .column("NAME", VARCHAR(100).nullable(false))
                .column("EMAIL", VARCHAR(255))
                .column("CREATED_AT", TIMESTAMP.nullable(false).defaultValue(currentTimestamp()))
                .constraints(
                        constraint("PK_CUSTOMER").primaryKey("ID"),
                        constraint("CUSTOMER_CUSTOMER_NO").unique("EMAIL")
                )
                .execute();

        // 4) Tabelle ORDERS – ID per Sequence-Default + FK
        ctx.createTableIfNotExists(name("APP", "ORDERS"))
                .column("ID", BIGINT.nullable(false).defaultValue(sequence(name("APP", "ORDERS_SEQ"), BIGINT).nextval()))
                .column("CUSTOMER_ID", BIGINT.nullable(false))
                .column("ORDER_NO", VARCHAR(30).nullable(false))
                .column("ORDER_DATE", LOCALDATE.nullable(false).defaultValue(currentLocalDate()))
                .column("STATUS", TIMESTAMP.nullable(false).defaultValue(currentTimestamp()))
                .constraints(
                        constraint("PK_ORDERS").primaryKey("ID"),
                        constraint("ORDERS_CUSTOMER_NO").unique("ORDER_NO"),
                        constraint("FK_ORDERS_CUSTOMER")
                                .foreignKey("CUSTOMER_ID")
                                .references(name("APP", "CUSTOMER"), name("ID"))
                                .onDeleteCascade()
                                .onUpdateRestrict()
                )
                .execute();

        // 5) Tabelle ORDER_ITEM – ID per Sequence-Default + FK
        ctx.createTableIfNotExists(name("APP", "ORDER_ITEM"))
                .column("ID", BIGINT.nullable(false))
                .column("ORDER_ID", BIGINT.nullable(false))
                .column("POSITION_NO", INTEGER.nullable(false))
                .column("PRODUCT_CODE", VARCHAR(50).nullable(false))
                .column("QUANTITY", INTEGER.nullable(false))
                .column("UNIT_PRICE", DECIMAL(12, 2).nullable(false))
                .constraints(
                        constraint("FK_ORDERITEM_ORDERS")
                                .foreignKey("ORDER_ID")
                                .references(name("APP", "ORDERS"), name("ID"))
                                .onDeleteCascade()
                                .onUpdateRestrict(),
                        constraint("CK_ORDER_ITEM_QTY").check(field(name("QUANTITY"), INTEGER).gt(0)),
                        constraint("CK_ORDER_ITEM_PRICE").check(field(name("UNIT_PRICE"), DECIMAL).gt(BigDecimal.ZERO)),
                        constraint("UQ_ORDERITEM_ORDER_POS").unique("ORDER_ID", "POSITION_NO")
                )
                .execute();

        // 6) Index
        ctx.createIndexIfNotExists("IDX_ORDERS_CUSTOMER")
                .on(table(name("APP", "ORDERS")), field(name("CUSTOMER_ID")))
                .execute();
        ctx.createIndexIfNotExists("IX_ORDERITEM_ORDER_ID")
                .on(table(name("APP", "ORDER_ITEM")), field(name("ORDER_ID")))
                .execute();
    }
}
