package net.progruzovik.bus.message;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;

public interface MailHandler {

    void handle(Message message) throws IOException, MessagingException;
}
