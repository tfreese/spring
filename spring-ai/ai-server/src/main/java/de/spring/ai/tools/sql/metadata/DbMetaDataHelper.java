package de.spring.ai.tools.sql.metadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Thomas Freese
 */
public final class DbMetaDataHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(DbMetaDataHelper.class);

    private final DataSource dataSource;
    private final JsonMapper jsonMapper;

    public DbMetaDataHelper(final DataSource dataSource, final JsonMapper jsonMapper) {
        super();

        this.dataSource = Objects.requireNonNull(dataSource, "dataSource required");
        this.jsonMapper = Objects.requireNonNull(jsonMapper, "jsonMapper required");
    }

    public DbMetaData extractMetadata() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            final DatabaseMetaData metadata = connection.getMetaData();
            final List<TableInfo> tablesInfo = new ArrayList<>();

            try (ResultSet tables = metadata.getTables(null, "PUBLIC", null, new String[]{"TABLE"})) {
                while (tables.next()) {
                    final String tableName = tables.getString("TABLE_NAME");
                    final String tableDescription = tables.getString("REMARKS");
                    final String tableCatalog = tables.getString("TABLE_CAT");
                    final String tableSchema = tables.getString("TABLE_SCHEM");

                    final List<ColumnInfo> columnsInfo = new ArrayList<>();
                    
                    tablesInfo.add(new TableInfo(tableName, tableDescription, tableCatalog, tableSchema, columnsInfo));

                    try (ResultSet columns = metadata.getColumns(null, null, tableName, null)) {
                        while (columns.next()) {
                            final String columnName = columns.getString("COLUMN_NAME");
                            final String datatype = columns.getString("TYPE_NAME");
                            final String columnDescription = columns.getString("REMARKS");

                            columnsInfo.add(new ColumnInfo(columnName, datatype, columnDescription));
                        }
                    }
                }
            }

            LOGGER.info("Database schema:");

            tablesInfo.forEach(ti -> LOGGER.info("\t{}", ti));

            return new DbMetaData(tablesInfo);
        }
    }

    public String extractMetadataJson() throws SQLException {
        final DbMetaData metadata = extractMetadata();

        return jsonMapper.writeValueAsString(metadata);
    }
}
