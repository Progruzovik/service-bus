package net.progruzovik.bus.message.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class RestMessageDto {

    private final @NonNull String from;
    private final @NonNull String to;
    private final @NonNull String subject;
    private final @Nullable String data;

    public RestMessageDto(@JsonProperty("from") @NonNull String from,
                          @JsonProperty("to") @NonNull String to,
                          @JsonProperty("subject") @NonNull String subject,
                          @JsonProperty("data") @Nullable String data) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.data = data;
    }

    @NonNull
    public String getFrom() {
        return from;
    }

    @NonNull
    public String getTo() {
        return to;
    }

    @NonNull
    public String getSubject() {
        return subject;
    }

    @Nullable
    public String getData() {
        return data;
    }
}
