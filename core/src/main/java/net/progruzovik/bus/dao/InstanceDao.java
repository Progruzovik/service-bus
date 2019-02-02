package net.progruzovik.bus.dao;

import net.progruzovik.bus.replication.model.Subscription;
import net.progruzovik.bus.replication.model.SubscriptionType;

import java.util.List;

public interface InstanceDao {

    Boolean isInstanceInitialized(String address);

    Boolean isEntityExists(String entityName);

    List<String> getAddresses();

    List<Subscription> getSubscriptions(String entityName);

    SubscriptionType getSubscriptionType(String address, String entityName);

    void addInstance(String address);

    void removeInstance(String address);

    List<String> getEntityNames();

    void addEntity(String entityName);

    void removeEntity(String entityName);

    void updateInstanceSubscription(Subscription subscription);
}
