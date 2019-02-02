package net.progruzovik.bus.replication.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.progruzovik.bus.replication.Replicator;

import java.io.IOException;
import java.util.Map;

public class Row {

    public static <T> Row fromData(String entityName, ObjectMapper mapper,
                                   T data, boolean isBinariesChanged) throws IOException {
        final byte[] plainDataBytes = mapper.writerWithView(Replicator.PlainData.class).writeValueAsBytes(data);
        final TypeReference<Map<String, Object>> plainDataType = new TypeReference<Map<String, Object>>() { };
        final Map<String, Object> plainData = mapper.readValue(plainDataBytes, plainDataType);
        final byte[] linksBytes = mapper.writerWithView(Replicator.BinaryLink.class).writeValueAsBytes(data);
        final Map<String, String> binaryLinks = mapper.readValue(linksBytes,
                new TypeReference<Map<String, String>>() { });
        return new Row(isBinariesChanged, entityName, plainData, binaryLinks);
    }

    public static <T> Row fromData(String entityName, ObjectMapper mapper, T data) throws IOException {
        return fromData(entityName, mapper, data, false);
    }

    private final boolean isBinariesChanged;
    private final String entityName;

    private final Map<String, Object> plainData;
    private final Map<String, String> binaryLinks;

    public Row(@JsonProperty("isBinariesChanged") boolean isBinariesChanged,
               @JsonProperty("entityName") String entityName,
               @JsonProperty("plainData") Map<String, Object> plainData,
               @JsonProperty("binaryLinks") Map<String, String> binaryLinks) {
        this.isBinariesChanged = isBinariesChanged;
        this.entityName = entityName;
        this.plainData = plainData;
        this.binaryLinks = binaryLinks;
    }

    @JsonProperty("isBinariesChanged")
    public boolean isBinariesChanged() {
        return isBinariesChanged;
    }

    public String getEntityName() {
        return entityName;
    }

    public Map<String, Object> getPlainData() {
        return plainData;
    }

    public Map<String, String> getBinaryLinks() {
        return binaryLinks;
    }
}
