package net.progruzovik.bus.replication;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.progruzovik.bus.message.model.Subject;
import net.progruzovik.bus.replication.exception.*;
import net.progruzovik.bus.replication.model.Entity;
import net.progruzovik.bus.replication.model.Row;
import net.progruzovik.bus.replication.model.Subscription;
import net.progruzovik.bus.replication.model.SubscriptionType;
import net.progruzovik.bus.dao.EntityDao;
import net.progruzovik.bus.dao.InstanceDao;
import net.progruzovik.bus.message.Writer;
import net.progruzovik.bus.message.model.DataMessage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ReplicationService implements Replicator {

    private final ObjectMapper mapper;
    private final Writer writer;
    private final InstanceDao instanceDao;
    private final EntityDao entityDao;

    public ReplicationService(ObjectMapper mapper, Writer writer, InstanceDao instanceDao, EntityDao entityDao) {
        this.mapper = mapper;
        this.writer = writer;
        this.instanceDao = instanceDao;
        this.entityDao = entityDao;
    }

    @Override
    public void addEntity(Entity entity) throws ReplicationException {
        initializeEntity(entity.getName(), true);
        entityDao.createEntity(entity);
        writer.broadcastMessage(new DataMessage<>(Subject.ADD_ENTITY, entity.getName()));
    }

    @Override
    public void initializeEntity(String entityName, boolean isOwner) throws ReplicationException {
        if (instanceDao.isEntityExists(entityName)) throw new ExistingEntityException(entityName);
        instanceDao.addEntity(entityName);
        updateSubscription(new Subscription(entityName, isOwner ? SubscriptionType.OWNER : SubscriptionType.COMMON));
    }

    @Override
    public void updateSubscription(Subscription subscription) throws ReplicationException {
        subscription.setAddress(writer.getAddress());
        final String entityName = subscription.getEntityName();
        if (!instanceDao.isEntityExists(entityName)) throw new AbsentEntityException(entityName);
        final SubscriptionType previousType = instanceDao.getSubscriptionType(writer.getAddress(), entityName);
        if (!subscription.canBeUpdatedFromType(previousType)) {
            throw new InvalidSubscriptionUpdateException(previousType, subscription.getType());
        }
        instanceDao.updateInstanceSubscription(subscription);
        if (subscription.getType() == SubscriptionType.NONE) {
            entityDao.dropEntity(entityName);
        }
        writer.broadcastMessage(new DataMessage<>(Subject.UPDATE_SUBSCRIPTION, subscription));
    }

    @Override
    public <T> void addRow(String entityName, T rowData, Map<String, Path> binaryPaths) throws IOException {
        final SubscriptionType currentType = instanceDao.getSubscriptionType(writer.getAddress(), entityName);
        if (currentType != SubscriptionType.OWNER) {
            throw new ProhibitedActionException(currentType, "add row", entityName);
        }
        final Row row = Row.fromData(entityName, mapper, rowData, binaryPaths != null);
        entityDao.addRowToEntity(row, binaryPaths);
        writer.broadcastMessage(new DataMessage<>(Subject.ADD_ROW, row, binaryPaths));
    }

    @Override
    public <T> void addRow(String entityName, T rowData, String binaryName, Path binaryPath) throws IOException {
        final Map<String, Path> binaryWrapper = new HashMap<>(1);
        binaryWrapper.put(binaryName, binaryPath);
        addRow(entityName, rowData, binaryWrapper);
    }

    @Override
    public <T> void addRow(String entityName, T rowData) throws IOException {
        addRow(entityName, rowData, null);
    }

    @Override
    public <T> void removeRow(String entityName, T rowData, boolean isBinariesChanged) throws IOException {
        final SubscriptionType currentType = instanceDao.getSubscriptionType(writer.getAddress(), entityName);
        if (currentType != SubscriptionType.OWNER) {
            throw new ProhibitedActionException(currentType, "remove row", entityName);
        }
        final Row row = Row.fromData(entityName, mapper, rowData, isBinariesChanged);
        entityDao.removeRowFromEntity(row);
        writer.broadcastMessage(new DataMessage<>(Subject.REMOVE_ROW, row));
    }

    @Override
    public <T> void removeRow(String entityName, T rowData) throws IOException {
        removeRow(entityName, rowData, true);
    }

    @Override
    public void removeEntity(String name) throws ReplicationException {
        if (!instanceDao.isEntityExists(name)) throw new AbsentEntityException(name);
        final SubscriptionType currentType = instanceDao.getSubscriptionType(writer.getAddress(), name);
        if (currentType != SubscriptionType.OWNER) {
            throw new ProhibitedActionException(currentType, "remove entity", name);
        }
        instanceDao.removeEntity(name);
        entityDao.dropEntity(name);
        writer.broadcastMessage(new DataMessage<>(Subject.REMOVE_ENTITY, name));
    }
}
