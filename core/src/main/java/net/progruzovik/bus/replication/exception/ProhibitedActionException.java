package net.progruzovik.bus.replication.exception;

import net.progruzovik.bus.replication.model.SubscriptionType;

public class ProhibitedActionException extends ReplicationException {

    public ProhibitedActionException(SubscriptionType subscriptionType, String action, String target) {
        super(String.format("Instance with subscription type %s can't %s \"%s\"!", subscriptionType, action, target));
    }
}
