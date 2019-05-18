package Lab7.Commands;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class EmailSender {
    public String sendEmail(String email, String password){
        try {
            final Properties properties = new Properties();
            properties.load(new FileInputStream("mail.properties"));

            Session mailSession = Session.getDefaultInstance(properties);
            MimeMessage message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress("subscribeonpornhub@gmail.com"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject("Password");
            message.setText("This is your password. Don't show it to anybody\n" + password);

            Transport tr = mailSession.getTransport();
            tr.connect("subscribeonpornhub@gmail.com", "yesbaby11");
            tr.sendMessage(message, message.getAllRecipients());
            tr.close();
            return "Password has been sent";
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return "Failed to send a password";
    }

}
