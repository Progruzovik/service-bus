package net.progruzovik.bus.message;

import net.progruzovik.bus.message.model.SerializedMessage;

public interface MessageSender {

    String getAddress();

    void sendMessage(String to, SerializedMessage message);
}
