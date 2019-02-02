package net.progruzovik.bus.message;

import net.progruzovik.bus.dao.EntityDao;
import net.progruzovik.bus.dao.InstanceDao;
import net.progruzovik.bus.message.model.DataMessage;
import net.progruzovik.bus.message.model.SerializedMessage;
import net.progruzovik.bus.message.model.Subject;
import net.progruzovik.bus.replication.model.Entity;
import net.progruzovik.bus.replication.model.Subscription;
import net.progruzovik.bus.replication.model.SubscriptionType;

import java.io.IOException;

public class SubscriptionUpdater implements Reader {

    private final Writer writer;

    private final InstanceDao instanceDao;
    private final EntityDao entityDao;

    public SubscriptionUpdater(Writer writer, InstanceDao instanceDao, EntityDao entityDao) {
        this.writer = writer;
        this.instanceDao = instanceDao;
        this.entityDao = entityDao;
    }

    @Override
    public void readMessage(String from, SerializedMessage message) throws IOException {
        final Subscription subscription = message.deserializeData(Subscription.class);
        subscription.setAddress(from);
        final String entityName = subscription.getEntityName();
        if (subscription.canBeUpdatedFromType(instanceDao.getSubscriptionType(from, entityName))) {
            instanceDao.updateInstanceSubscription(subscription);
            if (instanceDao.getSubscriptionType(writer.getAddress(), entityName) == SubscriptionType.OWNER) {
                if (subscription.getType() == SubscriptionType.COMMON) {
                    final Entity entity = new Entity(entityName, entityDao.getEntityColumns(entityName));
                    writer.writeMessage(from, new DataMessage<>(Subject.CREATE_ENTITY, entity));
                } else if (subscription.getType() == SubscriptionType.OWNER) {
                    final Subscription downgradedSubscription = new Subscription(writer.getAddress(),
                            entityName, SubscriptionType.COMMON);
                    instanceDao.updateInstanceSubscription(downgradedSubscription);
                }
            }
        } else {
            writer.responseWithError(from, message);
        }
    }
}
