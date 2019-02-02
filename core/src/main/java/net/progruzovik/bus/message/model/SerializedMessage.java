package net.progruzovik.bus.message.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class SerializedMessage extends DataMessage<String> {

    private final ObjectMapper mapper;

    public SerializedMessage(String subject, String data, Map<String, Path> attachments, ObjectMapper mapper) {
        super(subject, data, attachments);
        this.mapper = mapper;
    }

    public SerializedMessage(@JsonProperty("subject") String subject,
                             @JsonProperty("data") String data,
                             @JsonProperty("attachments") Map<String, Path> attachments) {
        this(subject, data, attachments, null);
    }

    public <T> T deserializeData(Class<T> clazz) throws IOException {
        return mapper != null ? mapper.readValue(getData(), clazz) : null;
    }

    @Override
    public String toString() {
        return String.format("[subject: \"%s\", data: \"%s\", attachments count: %d]",
                getSubject(), getData(), getAttachments() == null ? 0 : getAttachments().size());
    }
}
