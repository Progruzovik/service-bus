package net.progruzovik.bus.replication.model;

public class Column {

    private boolean nullable;
    private int size;

    private String name;
    private String type;

    public Column(String name, String type, boolean nullable, int size) {
        this.nullable = nullable;
        this.size = size;
        this.name = name;
        this.type = type;
    }

    public Column(String name, String type, boolean nullable) {
        this(name, type, nullable, 0);
    }

    public Column(String name, String type, int size) {
        this(name, type, false, size);
    }

    public Column(String name, String type) {
        this(name, type, false, 0);
    }

    public Column() { }

    public boolean isNullable() {
        return nullable;
    }

    public int getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
