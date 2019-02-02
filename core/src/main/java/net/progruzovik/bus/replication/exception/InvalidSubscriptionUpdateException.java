package net.progruzovik.bus.replication.exception;

import net.progruzovik.bus.replication.model.SubscriptionType;

public class InvalidSubscriptionUpdateException extends ReplicationException {

    public InvalidSubscriptionUpdateException(SubscriptionType oldType, SubscriptionType newType) {
        super(String.format("Subscription with the type %s can't be updated to the type %s!", oldType, newType));
    }
}
