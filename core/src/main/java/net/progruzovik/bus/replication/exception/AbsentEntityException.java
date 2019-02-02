package net.progruzovik.bus.replication.exception;

public class AbsentEntityException extends ReplicationException {

    public AbsentEntityException(String entityName) {
        super(String.format("Entity \"%s\" not found!", entityName));
    }
}
