package net.progruzovik.bus.message;

import net.progruzovik.bus.message.model.SerializedMessage;

import java.io.IOException;

@FunctionalInterface
public interface Reader {

    void readMessage(String from, SerializedMessage message) throws IOException;
}
