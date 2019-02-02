package net.progruzovik.bus.replication.exception;

public class ExistingEntityException extends ReplicationException {

    public ExistingEntityException(String entityName) {
        super(String.format("Entity \"%s\" already exists!", entityName));
    }
}
