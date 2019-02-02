package net.progruzovik.bus.message;

import net.progruzovik.bus.message.model.SerializedMessage;

import java.io.IOException;

public interface BusHandler {

    void handleMessage(String from, SerializedMessage message) throws IOException;
}
