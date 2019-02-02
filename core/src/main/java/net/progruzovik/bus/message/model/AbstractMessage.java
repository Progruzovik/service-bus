package net.progruzovik.bus.message.model;

import java.nio.file.Path;
import java.util.Map;

public abstract class AbstractMessage<T> {

    private final String subject;

    AbstractMessage(String subject) {
        this.subject = subject;
    }

    public abstract boolean hasData();

    public abstract boolean hasAttachments();

    public String getSubject() {
        return subject;
    }

    public abstract T getData();

    public abstract Map<String, Path> getAttachments();
}
