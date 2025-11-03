package rentalmanagementsystem.rent_management;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Random;

public class EmailSender {

    // Sends OTP email to a given recipient
    public static void sendOTP(String recipientEmail, String roomNo, String otp) {
        // Sender email credentials
        final String senderEmail = "brotres091205@gmail.com";
        final String senderPassword = "lbynbsxztcuflcfq"; // Use app password (not your real Gmail password)

        // SMTP server configuration for Gmail
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        // Create a session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Room OTP Confirmation");

            String emailBody = """
                Dear Tenant,
                
                Your OTP for Room %s is: %s
                
                Please use this code to link your account upon creation of your 
                account.
                
                Regards,
                Rent Management System
                """.formatted(roomNo, otp);

            message.setText(emailBody);

            Transport.send(message);
            System.out.println("✅ OTP email sent successfully to " + recipientEmail);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to send OTP email.");
        }
    }

    public static String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
