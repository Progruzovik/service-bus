package net.progruzovik.bus.message;

import org.springframework.lang.Nullable;

import java.io.IOException;
import java.time.Instant;

public interface RestReceiver {

    void receiveMessages(@Nullable Instant fromTime) throws IOException;

    void receiveMessages() throws IOException;
}
