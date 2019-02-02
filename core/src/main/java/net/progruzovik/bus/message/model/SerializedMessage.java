package net.progruzovik.bus.message.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;
import java.util.Map;

public class SerializedMessage extends DataMessage<String> {

    public SerializedMessage(@JsonProperty("subject") String subject,
                             @JsonProperty("data") String data,
                             @JsonProperty("attachments") Map<String, Path> attachments) {
        super(subject, data, attachments);
    }

    @Override
    public String toString() {
        return String.format("[subject: \"%s\", data: \"%s\", attachments count: %d]",
                getSubject(), getData(), getAttachments() == null ? 0 : getAttachments().size());
    }
}
