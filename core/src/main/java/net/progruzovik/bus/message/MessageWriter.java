package net.progruzovik.bus.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.progruzovik.bus.dao.InstanceDao;
import net.progruzovik.bus.message.model.AbstractMessage;
import net.progruzovik.bus.message.model.DataMessage;
import net.progruzovik.bus.message.model.SerializedMessage;
import net.progruzovik.bus.message.model.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MessageWriter implements Writer {

    private static final Logger log = LoggerFactory.getLogger(MessageWriter.class);

    private final ObjectMapper mapper;
    private final Sender sender;
    private final InstanceDao instanceDao;

    public MessageWriter(ObjectMapper mapper, Sender sender, InstanceDao instanceDao) {
        this.mapper = mapper;
        this.sender = sender;
        this.instanceDao = instanceDao;
    }

    @Override
    public String getAddress() {
        return sender.getAddress();
    }

    @Override
    public <T> void writeMessage(String to, AbstractMessage<T> message) {
        sender.sendMessage(to, serializeMessage(message));
    }

    @Override
    public void responseWithError(String to, SerializedMessage originalMessage) {
        writeMessage(to, new DataMessage<>(Subject.ERROR_RESPONSE, originalMessage));
    }

    @Override
    public <T> void broadcastMessage(AbstractMessage<T> message) {
        final SerializedMessage serializedMessage = serializeMessage(message);
        for (final String address : instanceDao.getAddresses()) {
            if (!address.equals(getAddress())) {
                sender.sendMessage(address, serializedMessage);
            }
        }
    }

    private <T> SerializedMessage serializeMessage(AbstractMessage<T> message) {
        String serializedData = null;
        if (message.hasData()) {
            try {
                serializedData = mapper.writeValueAsString(message.getData());
            } catch (IOException e) {
                log.error("", e);
            }
        }
        return new SerializedMessage(message.getSubject(), serializedData, message.getAttachments(), mapper);
    }
}
