package net.progruzovik.bus.message;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.time.Instant;

public interface RestReceiver {

    @NonNull
    Instant receiveMessages(@Nullable Instant fromTime);

    @NonNull
    Instant receiveMessages() throws IOException;
}
