package net.progruzovik.bus.message;

import net.progruzovik.bus.message.model.SerializedMessage;

public interface Sender {

    String getAddress();

    void sendMessage(String to, SerializedMessage message);
}
