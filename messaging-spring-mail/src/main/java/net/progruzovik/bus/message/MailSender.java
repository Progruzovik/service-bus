package net.progruzovik.bus.message;

import net.progruzovik.bus.message.model.SerializedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.file.Path;
import java.util.Map;

public class MailSender implements MessageSender {

    private static final Logger log = LoggerFactory.getLogger(MailSender.class);

    private final String address;
    private final JavaMailSender sender;

    public MailSender(String address, JavaMailSender sender) {
        this.address = address;
        this.sender = sender;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public void sendMessage(String to, SerializedMessage message) {
        final MimeMessage mailMessage = sender.createMimeMessage();
        try {
            final MimeMessageHelper helper = new MimeMessageHelper(mailMessage, message.hasAttachments());
            helper.setFrom(address);
            helper.setTo(to);
            helper.setSubject(message.getSubject());
            helper.setText(message.getData() != null ? message.getData() : "");
            if (message.hasAttachments()) {
                for (final Map.Entry<String, Path> attachment : message.getAttachments().entrySet()) {
                    helper.addAttachment(attachment.getKey(), attachment.getValue().toFile());
                }
            }
        } catch (MessagingException e) {
            log.error("", e);
        }
        sender.send(mailMessage);
    }
}
