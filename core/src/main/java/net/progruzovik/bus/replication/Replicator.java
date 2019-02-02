package net.progruzovik.bus.replication;

import net.progruzovik.bus.replication.exception.ReplicationException;
import net.progruzovik.bus.replication.model.Entity;
import net.progruzovik.bus.replication.model.Subscription;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public interface Replicator {

    class PlainData { }

    class BinaryLink { }

    void addEntity(Entity entity) throws ReplicationException;

    void updateSubscription(Subscription subscription) throws ReplicationException;

    <T> void addRow(String entityName, T rowData, Map<String, Path> binaryPaths) throws IOException;

    <T> void addRow(String entityName, T rowData, String binaryName, Path binaryPath) throws IOException;

    <T> void addRow(String entityName, T rowData) throws IOException;

    <T> void removeRow(String entityName, T rowData, boolean isBinariesChanged) throws IOException;

    <T> void removeRow(String entityName, T rowData) throws IOException;

    void removeEntity(String name) throws ReplicationException;
}
