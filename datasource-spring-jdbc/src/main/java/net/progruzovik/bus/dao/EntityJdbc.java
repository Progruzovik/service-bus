package net.progruzovik.bus.dao;

import net.progruzovik.bus.replication.model.Column;
import net.progruzovik.bus.replication.model.Entity;
import net.progruzovik.bus.replication.model.Row;
import net.progruzovik.bus.util.EntityNameConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.ResultSetMetaData;
import java.util.*;

public class EntityJdbc implements EntityDao {

    static final int FIRST_ENTITY_COLUMN = 2;
    private static final String PRIMARY_KEY = "busRowId";

    private final EntityNameConverter converter;
    private final BinaryDao binaryDao;
    private final JdbcTemplate jdbcTemplate;

    public EntityJdbc(EntityNameConverter converter, BinaryDao binaryDao, JdbcTemplate jdbcTemplate) {
        this.converter = converter;
        this.binaryDao = binaryDao;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Nullable
    @Override
    public List<Column> getEntityColumns(String name) {
        return jdbcTemplate.query(String.format("SELECT TOP 1 * FROM %s", converter.toDatabase(name)), rs -> {
            final List<Column> result = new ArrayList<>(rs.getMetaData().getColumnCount());
            for (int i = FIRST_ENTITY_COLUMN; i <= rs.getMetaData().getColumnCount(); i++) {
                final boolean isNullable = rs.getMetaData().isNullable(i) != ResultSetMetaData.columnNoNulls;
                result.add(new Column(rs.getMetaData().getColumnName(i),
                        rs.getMetaData().getColumnTypeName(i), isNullable, rs.getMetaData().getPrecision(i)));
            }
            return result;
        });
    }

    @Override
    public void createEntity(Entity entity) {
        final StringBuilder query = new StringBuilder();
        query.append("CREATE TABLE ");
        query.append(converter.toDatabase(entity.getName()));
        query.append("(");
        query.append(PRIMARY_KEY);
        query.append(" INT AUTO_INCREMENT PRIMARY KEY");
        for (final Column column : entity.getColumns()) {
            query.append(", ");
            query.append(column.getName());
            query.append(" ");
            query.append(column.getType());
            query.append("(");
            query.append(column.getSize());
            query.append(")");
            if (!column.isNullable()) {
                query.append(" NOT NULL");
            }
        }
        query.append(")");
        jdbcTemplate.update(query.toString());
    }

    @Override
    public void dropEntity(String name) {
        jdbcTemplate.update(String.format("DROP TABLE %s", converter.toDatabase(name)));
    }

    @Override
    public void addRowToEntity(Row row, Map<String, Path> binaryPaths) throws IOException {
        final String entityName = converter.toDatabase(row.getEntityName());
        final StringBuilder query = new StringBuilder(String.format("INSERT INTO %s(", entityName));
        final Set<String> columnNames = new HashSet<>();
        final List<Object> values = new ArrayList<>();
        for (final Map.Entry<String, Object> column : row.getPlainData().entrySet()) {
            if (column.getValue() != null) {
                query.append(converter.toDatabase(column.getKey()));
                query.append(", ");
                columnNames.add(column.getKey());
                values.add(column.getValue());
            }
        }
        for (final Map.Entry<String, String> link : row.getBinaryLinks().entrySet()) {
            if (link.getValue() != null) {
                if (row.isBinariesChanged()) {
                    if (binaryPaths == null || !binaryPaths.containsKey(link.getValue())) {
                        throw new IOException(String.format("Can't find path to the binary with link \"%s\"!",
                                link.getValue()));
                    }
                    final Path newPath = binaryDao.addBinary(link.getValue(), binaryPaths.get(link.getValue()));
                    binaryPaths.put(link.getValue(), newPath);
                }
                if (!columnNames.contains(link.getKey())) {
                    query.append(converter.toDatabase(link.getKey()));
                    query.append(", ");
                    columnNames.add(link.getKey());
                    values.add(link.getValue());
                }
            }
        }
        query.delete(query.length() - 2, query.length());
        query.append(") VALUES (");
        for (final Object value : values) {
            final boolean isStringValue = value instanceof String;
            if (isStringValue) {
                query.append('\'');
            }
            query.append(value);
            if (isStringValue) {
                query.append('\'');
            }
            query.append(", ");
        }
        query.delete(query.length() - 2, query.length());
        query.append(")");
        jdbcTemplate.update(query.toString());
    }

    @Override
    public void removeRowFromEntity(Row row) throws IOException {
        final StringBuilder query = new StringBuilder(String.format("DELETE FROM %s WHERE",
                converter.toDatabase(row.getEntityName())));
        for (final Map.Entry<String, Object> column : row.getPlainData().entrySet()) {
            if (column.getValue() != null) {
                query.append(String.format(" %s = '%s' AND",
                        converter.toDatabase(converter.toDatabase(column.getKey())), column.getValue()));
            }
        }
        for (final Map.Entry<String, String> link : row.getBinaryLinks().entrySet()) {
            if (link.getValue() != null) {
                if (row.isBinariesChanged()) {
                    binaryDao.removeBinary(link.getValue());
                }
                query.append(String.format(" %s = '%s' AND", converter.toDatabase(link.getKey()), link.getValue()));
            }
        }
        query.delete(query.length() - 4, query.length());
        jdbcTemplate.update(query.toString());
    }
}
