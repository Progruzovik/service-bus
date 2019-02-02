package net.progruzovik.bus.replication.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Subscription {

    private String address;
    private final String entityName;
    private final SubscriptionType type;

    public Subscription(@JsonProperty("address") String address,
                        @JsonProperty("entityName") String entityName,
                        @JsonProperty("type") SubscriptionType type) {
        this.address = address;
        this.entityName = entityName;
        this.type = type;
    }

    public Subscription(String entityName, SubscriptionType type) {
        this(null, entityName, type);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEntityName() {
        return entityName;
    }

    public SubscriptionType getType() {
        return type;
    }

    public boolean canBeUpdatedFromType(SubscriptionType previousType) {
        return previousType == SubscriptionType.NONE && type == SubscriptionType.COMMON
                || previousType == SubscriptionType.COMMON && (type == SubscriptionType.NONE || type == SubscriptionType.OWNER);
    }
}
