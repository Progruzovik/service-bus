package net.progruzovik.bus.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.progruzovik.bus.message.model.EmptyMessage;
import net.progruzovik.bus.message.model.Subject;
import net.progruzovik.bus.replication.model.Entity;
import net.progruzovik.bus.replication.model.Row;
import net.progruzovik.bus.replication.model.Subscription;
import net.progruzovik.bus.replication.model.SubscriptionType;
import net.progruzovik.bus.dao.EntityDao;
import net.progruzovik.bus.dao.InstanceDao;
import net.progruzovik.bus.message.model.SerializedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MessageHandler implements BusHandler {

    private static final Logger log = LoggerFactory.getLogger(MessageHandler.class);

    private final Map<String, Reader> readers = new HashMap<>();
    private final Reader errorResponder;

    public MessageHandler(ObjectMapper mapper, Writer writer, InstanceDao instanceDao,
                          EntityDao entityDao, String neighbor, Map<String, Reader> customReaders) {
        readers.put(Subject.INIT_INSTANCE.toString(), new InstanceInitializer(writer, instanceDao));
        readers.put(Subject.ADD_INSTANCE.toString(), (f, m) ->
                instanceDao.addInstance(mapper.readValue(m.getData(), String.class)));
        readers.put(Subject.REMOVE_INSTANCE.toString(),(f, m) ->
                instanceDao.removeInstance(mapper.readValue(m.getData(), String.class)));

        readers.put(Subject.ADD_ENTITY.toString(), (f, m) -> {
            final String entityName = mapper.readValue(m.getData(), String.class);
            if (instanceDao.isEntityExists(entityName)) {
                writer.responseWithError(f, m);
            } else {
                instanceDao.addEntity(entityName);
                instanceDao.updateInstanceSubscription(new Subscription(f, entityName, SubscriptionType.OWNER));
            }
        });
        readers.put(Subject.REMOVE_ENTITY.toString(), (f, m) -> {
            final String entityName = mapper.readValue(m.getData(), String.class);
            if (instanceDao.getSubscriptionType(f, entityName) == SubscriptionType.OWNER) {
                if (instanceDao.getSubscriptionType(writer.getAddress(), entityName) != SubscriptionType.NONE) {
                    entityDao.dropEntity(entityName);
                }
                instanceDao.removeEntity(entityName);
            } else {
                writer.responseWithError(f, m);
            }
        });

        readers.put(Subject.UPDATE_SUBSCRIPTION.toString(), new SubscriptionUpdater(mapper, writer, instanceDao, entityDao));
        readers.put(Subject.CREATE_ENTITY.toString(), (f, m) -> {
            final Entity entity = mapper.readValue(m.getData(), Entity.class);
            if (instanceDao.getSubscriptionType(f, entity.getName()) == SubscriptionType.OWNER) {
                entityDao.createEntity(entity);
            } else {
                writer.responseWithError(f, m);
            }
        });
        readers.put(Subject.ADD_ROW.toString(), (f, m) ->
                entityDao.addRowToEntity(mapper.readValue(m.getData(), Row.class), m.getAttachments()));
        readers.put(Subject.REMOVE_ROW.toString(), (f, m) ->
                entityDao.removeRowFromEntity(mapper.readValue(m.getData(), Row.class)));

        readers.put(Subject.ECHO_REQUEST.toString(), (f, m) -> {
            log.info("Received echo request from {}", f);
            writer.writeMessage(f, new EmptyMessage(Subject.ECHO_RESPONSE));
        });
        readers.put(Subject.ECHO_RESPONSE.toString(), (f, m) -> log.info("Received echo response from {}", f));
        readers.put(Subject.ERROR_RESPONSE.toString(), (f, m) -> {
            final SerializedMessage originalMessage = mapper.readValue(m.getData(), SerializedMessage.class);
            log.error("Received error response from {}. Original message: {}", f, originalMessage);
        });
        if (customReaders != null && !customReaders.isEmpty()) {
            for (final Map.Entry<String, Reader> reader : customReaders.entrySet()) {
                if (readers.containsKey(reader.getKey())) throw new DuplicateCustomReaderException(reader.getKey());
                readers.put(reader.getKey(), reader.getValue());
            }
        }
        errorResponder = (f, m) -> {
            log.error("There is no reader for message with subject \"{}\" (received from {})!", m.getSubject(), f);
            writer.responseWithError(f, m);
        };

        instanceDao.addInstance(writer.getAddress());
        if (neighbor != null && !neighbor.isEmpty()) {
            instanceDao.addInstance(neighbor);
            writer.writeMessage(neighbor, new EmptyMessage(Subject.INIT_INSTANCE));
        }
    }

    public MessageHandler(ObjectMapper mapper, Writer writer, InstanceDao instanceDao, EntityDao entityDao, String neighbor) {
        this(mapper, writer, instanceDao, entityDao, neighbor, null);
    }

    public MessageHandler(ObjectMapper mapper, Writer writer, InstanceDao instanceDao,
                          EntityDao entityDao, Map<String, Reader> customReaders) {
        this(mapper, writer, instanceDao, entityDao, null, customReaders);
    }

    public MessageHandler(ObjectMapper mapper, Writer writer, InstanceDao instanceDao, EntityDao entityDao) {
        this(mapper, writer, instanceDao, entityDao, null, null);
    }

    @Override
    public void handleMessage(String from, SerializedMessage message) throws IOException {
        log.debug("Received message from {} with subject \"{}\"", from, message.getSubject());
        readers.getOrDefault(message.getSubject(), errorResponder).readMessage(from, message);
    }
}
