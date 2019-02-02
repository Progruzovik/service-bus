package net.progruzovik.bus.replication.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Entity {

    private final String name;
    private final List<Column> columns;

    public Entity(@JsonProperty("name") String name,
                  @JsonProperty("columns") List<Column> columns) {
        this.name = name;
        this.columns = columns;
    }

    public String getName() {
        return name;
    }

    public List<Column> getColumns() {
        return columns;
    }
}
