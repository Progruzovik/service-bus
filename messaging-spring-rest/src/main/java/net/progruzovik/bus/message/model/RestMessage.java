package net.progruzovik.bus.message.model;

import org.springframework.lang.NonNull;

public class RestMessage {

    private final @NonNull String from;
    private final @NonNull String to;
    private final @NonNull String subject;
    private final @NonNull String data;

    public RestMessage(@NonNull String from, @NonNull String to, @NonNull String subject, @NonNull String data) {
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

    @NonNull
    public String getData() {
        return data;
    }
}
