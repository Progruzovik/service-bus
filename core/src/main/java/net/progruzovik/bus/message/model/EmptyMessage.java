package net.progruzovik.bus.message.model;

import java.nio.file.Path;
import java.util.Map;

public class EmptyMessage extends AbstractMessage<Object> {

    public EmptyMessage(String subject) {
        super(subject);
    }

    public EmptyMessage(Subject subject) {
        super(subject.toString());
    }

    @Override
    public boolean hasData() {
        return false;
    }

    @Override
    public boolean hasAttachments() {
        return false;
    }

    @Override
    public Object getData() {
        return null;
    }

    @Override
    public Map<String, Path> getAttachments() {
        return null;
    }
}
