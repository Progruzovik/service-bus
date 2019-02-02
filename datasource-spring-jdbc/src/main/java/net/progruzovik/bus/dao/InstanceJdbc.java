package net.progruzovik.bus.dao;

import net.progruzovik.bus.replication.model.Subscription;
import net.progruzovik.bus.replication.model.SubscriptionType;
import net.progruzovik.bus.util.EntityNameConverter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class InstanceJdbc implements InstanceDao {

    private final EntityNameConverter converter;
    private final JdbcTemplate jdbcTemplate;

    public InstanceJdbc(EntityNameConverter converter, JdbcTemplate jdbcTemplate) {
        this.converter = converter;
        this.jdbcTemplate = jdbcTemplate;
        jdbcTemplate.update("DROP TABLE IF EXISTS instance");
        jdbcTemplate.update("CREATE TABLE instance(address VARCHAR(50) PRIMARY KEY NOT NULL)");
    }

    @Nullable
    @Override
    public Boolean isInstanceInitialized(String address) {
        return jdbcTemplate.query("SELECT * FROM instance WHERE address = ?", ResultSet::next, address);
    }

    @Nullable
    @Override
    public Boolean isEntityExists(String entityName) {
        try {
            return jdbcTemplate.query(String.format("SELECT %s FROM instance",
                    converter.toDatabase(entityName)), ResultSet::next);
        } catch (DataAccessException e) {
            return false;
        }
    }

    @NonNull
    @Override
    public List<String> getAddresses() {
        return jdbcTemplate.query("SELECT address FROM instance", (rs, r) -> rs.getString("address"));
    }

    @Override
    public List<Subscription> getSubscriptions(String entityName) {
        final String databaseEntityName = converter.toDatabase(entityName);
        return jdbcTemplate.query(String.format("SELECT address, %s FROM instance", databaseEntityName), (rs, r) -> {
            final SubscriptionType subscriptionType = SubscriptionType.values()[rs.getInt(databaseEntityName)];
            return new Subscription(rs.getString("address"), entityName, subscriptionType);
        });
    }

    @Nullable
    @Override
    public SubscriptionType getSubscriptionType(String address, String entityName) {
        return jdbcTemplate.query(String.format("SELECT %s FROM instance WHERE address = ?",
                converter.toDatabase(entityName)), rs -> {
            rs.next();
            return SubscriptionType.values()[rs.getInt(1)];
        }, address);
    }

    @Override
    public void addInstance(String address) {
        jdbcTemplate.update("INSERT INTO instance(address) VALUES (?)", address);
    }

    @Override
    public void removeInstance(String address) {
        jdbcTemplate.update("DELETE FROM instance WHERE address = ?", address);
    }

    @Nullable
    @Override
    public List<String> getEntityNames() {
        return jdbcTemplate.query("SELECT TOP 1 * FROM instance", rs -> {
            final List<String> result = new ArrayList<>(rs.getMetaData().getColumnCount() - 1);
            for (int i = EntityJdbc.FIRST_ENTITY_COLUMN; i <= rs.getMetaData().getColumnCount(); i++) {
                result.add(converter.fromDatabase(rs.getMetaData().getColumnName(i)));
            }
            return result;
        });
    }

    @Override
    public void addEntity(String entityName) {
        jdbcTemplate.update(String.format("ALTER TABLE instance ADD %s INT NOT NULL DEFAULT 0",
                converter.toDatabase(entityName)));
    }

    @Override
    public void removeEntity(String entityName) {
        jdbcTemplate.update(String.format("ALTER TABLE instance DROP COLUMN %s", converter.toDatabase(entityName)));
    }

    @Override
    public void updateInstanceSubscription(Subscription subscription) {
        final String address = subscription.getAddress();
        jdbcTemplate.update(String.format("UPDATE instance SET %s = ? WHERE address = ?",
                converter.toDatabase(subscription.getEntityName())), subscription.getType().ordinal(), address);
    }
}
