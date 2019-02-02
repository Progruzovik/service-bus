package net.progruzovik.bus.replication.exception;

import java.io.IOException;

public class ReplicationException extends IOException {

    ReplicationException(String message) {
        super(message);
    }
}
