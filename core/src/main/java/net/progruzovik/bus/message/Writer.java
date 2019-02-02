package net.progruzovik.bus.message;

import net.progruzovik.bus.message.model.AbstractMessage;
import net.progruzovik.bus.message.model.SerializedMessage;

public interface Writer {

    String getAddress();

    <T> void writeMessage(String to, AbstractMessage<T> message);

    void responseWithError(String to, SerializedMessage originalMessage);

    <T> void broadcastMessage(AbstractMessage<T> message);
}
