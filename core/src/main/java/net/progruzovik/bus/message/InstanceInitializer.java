package net.progruzovik.bus.message;

import net.progruzovik.bus.dao.InstanceDao;
import net.progruzovik.bus.message.model.DataMessage;
import net.progruzovik.bus.message.model.SerializedMessage;
import net.progruzovik.bus.message.model.Subject;
import net.progruzovik.bus.replication.model.Subscription;
import net.progruzovik.bus.replication.model.SubscriptionType;

public class InstanceInitializer implements Reader {

    private final Writer writer;
    private final InstanceDao instanceDao;

    public InstanceInitializer(Writer writer, InstanceDao instanceDao) {
        this.writer = writer;
        this.instanceDao = instanceDao;
    }

    @Override
    public void readMessage(String from, SerializedMessage message) {
        if (instanceDao.isInstanceInitialized(from)) {
            writer.responseWithError(from, message);
        } else {
            writer.broadcastMessage(new DataMessage<>(Subject.ADD_INSTANCE, from));
            for (final String address : instanceDao.getAddresses()) {
                if (!address.equals(writer.getAddress())) {
                    writer.writeMessage(from, new DataMessage<>(Subject.ADD_INSTANCE, address));
                }
            }
            instanceDao.addInstance(from);
            for (final String entityName : instanceDao.getEntityNames()) {
                writer.writeMessage(from, new DataMessage<>(Subject.ADD_ENTITY, entityName));
                for (final Subscription subscription : instanceDao.getSubscriptions(entityName)) {
                    if (subscription.getType() != SubscriptionType.NONE) {
                        writer.writeMessage(from, new DataMessage<>(Subject.UPDATE_SUBSCRIPTION, subscription));
                    }
                }
            }
        }
    }
}
