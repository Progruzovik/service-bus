package net.progruzovik.bus.message;

import net.progruzovik.bus.message.model.SerializedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class BusMailHandler implements MailHandler {

    private static final Logger log = LoggerFactory.getLogger(BusMailHandler.class);

    private final BusHandler busHandler;

    public BusMailHandler(BusHandler busHandler) {
        this.busHandler = busHandler;
    }

    @Override
    public void handle(Message message) throws IOException, MessagingException {
        String text = null;
        Map<String, Path> attachments = null;
        try {
            final Deque<Part> parts = new ArrayDeque<>();
            parts.push(message);
            while (!parts.isEmpty()) {
                final Part part = parts.pop();
                if (part.getContentType().contains("multipart")) {
                    final Multipart multipart = (Multipart) part.getContent();
                    for (int i = 0; i < multipart.getCount(); i++) {
                        parts.push(multipart.getBodyPart(i));
                    }
                } else if (part.getContentType().contains("text")) {
                    text = part.getContent().toString();
                } else if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                    if (attachments == null) {
                        attachments = new HashMap<>();
                    }
                    final Path attachment = Files.createTempFile(part.getFileName(), null);
                    try (final InputStream inputStream = part.getInputStream()) {
                        Files.copy(inputStream, attachment, StandardCopyOption.REPLACE_EXISTING);
                    }
                    attachments.put(part.getFileName(), attachment);
                } else {
                    log.error("Unknown content type: {}", part.getContentType());
                }
            }
            final SerializedMessage serializedMessage = new SerializedMessage(message.getSubject(), text, attachments);
            busHandler.handleMessage(((InternetAddress) message.getFrom()[0]).getAddress(), serializedMessage);
        } finally {
            if (attachments != null) {
                for (final Map.Entry<String, Path> attachment : attachments.entrySet()) {
                    Files.deleteIfExists(attachment.getValue());
                }
            }
        }
    }
}
