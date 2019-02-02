package net.progruzovik.bus.message.model;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DataMessage<T> extends AbstractMessage<T> {

    private final T data;
    private final Map<String, Path> attachments;

    public DataMessage(String subject, T data, Map<String, Path> attachments) {
        super(subject);
        this.data = data;
        this.attachments = attachments;
    }

    public DataMessage(String subject, T data, String attachmentName, Path attachment) {
        super(subject);
        this.data = data;
        attachments = new HashMap<>();
        attachments.put(attachmentName, attachment);
    }

    public DataMessage(String subject, T data) {
        this(subject, data, null);
    }

    public DataMessage(Subject subject, T data, Map<String, Path> attachments) {
        this(subject.toString(), data, attachments);
    }

    public DataMessage(Subject subject, T data, String attachmentName, Path attachment) {
        this(subject.toString(), data, attachmentName, attachment);
    }

    public DataMessage(Subject subject, T data) {
        this(subject.toString(), data, null);
    }

    @Override
    public boolean hasData() {
        return data != null;
    }

    @Override
    public boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }

    @Override
    public T getData() {
        return data;
    }

    @Override
    public Map<String, Path> getAttachments() {
        return attachments;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) return false;
        final DataMessage<?> dataMessage = (DataMessage<?>) obj;
        return getSubject().equals(dataMessage.getSubject()) && data.equals(dataMessage.getData())
                && Objects.equals(attachments, dataMessage.getAttachments());
    }
}
