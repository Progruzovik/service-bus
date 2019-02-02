package net.progruzovik.bus.dao;

import net.progruzovik.bus.replication.model.Column;
import net.progruzovik.bus.replication.model.Entity;
import net.progruzovik.bus.replication.model.Row;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface EntityDao {

    List<Column> getEntityColumns(String name);

    void createEntity(Entity entity);

    void dropEntity(String name);

    void addRowToEntity(Row row, Map<String, Path> binaryPaths) throws IOException;

    void removeRowFromEntity(Row row) throws IOException;
}
