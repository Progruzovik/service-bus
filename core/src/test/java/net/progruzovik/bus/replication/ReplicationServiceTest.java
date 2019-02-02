package net.progruzovik.bus.replication;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.progruzovik.bus.dao.EntityDao;
import net.progruzovik.bus.dao.InstanceDao;
import net.progruzovik.bus.message.Writer;
import net.progruzovik.bus.message.model.DataMessage;
import net.progruzovik.bus.message.model.Subject;
import net.progruzovik.bus.replication.exception.ExistingEntityException;
import net.progruzovik.bus.replication.exception.ProhibitedActionException;
import net.progruzovik.bus.replication.model.Entity;
import net.progruzovik.bus.replication.model.Subscription;
import net.progruzovik.bus.replication.model.SubscriptionType;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ReplicationServiceTest {

    private final String ADDRESS = "testAddress";
    private final String ENTITY_NAME = "testEntity";

    private boolean isTestEntityAdded;
    private SubscriptionType subscriptionType;

    private final ObjectMapper mapper = new ObjectMapper();
    private final Writer writer = mock(Writer.class);
    private final InstanceDao instanceDao = mock(InstanceDao.class);
    private final EntityDao entityDao = mock(EntityDao.class);
    private final ReplicationService replicationService = new ReplicationService(mapper, writer, instanceDao, entityDao);

    @Before
    public void setUp() {
        isTestEntityAdded = false;
        subscriptionType = SubscriptionType.COMMON;

        reset(writer);
        when(writer.getAddress()).thenReturn(ADDRESS);
        reset(instanceDao);
        doAnswer(invocation -> isTestEntityAdded).when(instanceDao).isEntityExists(ENTITY_NAME);
        doAnswer(invocation -> subscriptionType).when(instanceDao).getSubscriptionType(ADDRESS, ENTITY_NAME);
        doAnswer(invocation -> isTestEntityAdded = true).when(instanceDao).addEntity(ENTITY_NAME);
        doAnswer(invocation -> {
            final Subscription subscription = invocation.getArgument(0);
            subscriptionType = subscription.getType();
            return null;
        }).when(instanceDao).updateInstanceSubscription(any());
        reset(entityDao);
    }

    @Test
    public void addNewEntity() throws Exception {
        subscriptionType = SubscriptionType.NONE;
        final Entity entity = new Entity(ENTITY_NAME, new ArrayList<>());
        replicationService.addEntity(entity);

        verify(instanceDao, times(3)).isEntityExists(entity.getName());
        verify(instanceDao).addEntity(entity.getName());
        verify(entityDao).createEntity(entity);
        verify(writer).broadcastMessage(new DataMessage<>(Subject.ADD_ENTITY, entity.getName()));
    }

    @Test(expected = ExistingEntityException.class)
    public void addExistingEntity() throws Exception {
        isTestEntityAdded = true;
        replicationService.addEntity(new Entity(ENTITY_NAME, new ArrayList<>()));
    }

    @Test
    public void changeOwner() throws Exception {
        isTestEntityAdded = true;
        final Subscription subscription = new Subscription(ENTITY_NAME, SubscriptionType.OWNER);
        replicationService.updateSubscription(subscription);

        verify(instanceDao).updateInstanceSubscription(subscription);
        verify(writer).broadcastMessage(new DataMessage<>(Subject.UPDATE_SUBSCRIPTION, subscription));
    }

    @Test
    public void unsubscribeFromEntity() throws Exception {
        isTestEntityAdded = true;
        final Subscription subscription = new Subscription(ADDRESS, ENTITY_NAME, SubscriptionType.NONE);
        replicationService.updateSubscription(subscription);

        verify(instanceDao).updateInstanceSubscription(subscription);
        verify(entityDao).dropEntity(ENTITY_NAME);
        verify(writer).broadcastMessage(new DataMessage<>(Subject.UPDATE_SUBSCRIPTION, subscription));
    }

    @Test(expected = ProhibitedActionException.class)
    public void addRowWithCommonSubscription() throws Exception {
        replicationService.addRow(ENTITY_NAME, new Object());
    }

    @Test
    public void addRowWithOwnerSubscription() throws Exception {
        when(instanceDao.getSubscriptionType(ADDRESS, ENTITY_NAME)).thenReturn(SubscriptionType.OWNER);
        final Map<String, String> rowData = new HashMap<>(1);
        rowData.put("testColumn", "testData");
        replicationService.addRow(ENTITY_NAME, rowData);

        verify(entityDao).addRowToEntity(any(), isNull());
        verify(writer).broadcastMessage(any());
    }

    @Test
    public void removeEntity() throws Exception {
        isTestEntityAdded = true;
        when(instanceDao.getSubscriptionType(ADDRESS, ENTITY_NAME)).thenReturn(SubscriptionType.OWNER);
        replicationService.removeEntity(ENTITY_NAME);

        verify(instanceDao).removeEntity(ENTITY_NAME);
        verify(entityDao).dropEntity(ENTITY_NAME);
        verify(writer).broadcastMessage(new DataMessage<>(Subject.REMOVE_ENTITY, ENTITY_NAME));
    }
}
