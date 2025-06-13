package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Value("${aws.ses.from-email}")
    private String fromEmail;
    
    @Value("${aws.ses.admin-email}")
    private String adminEmail;
    
    @Value("${aws.ses.smtp.host}")
    private String smtpHost;
    
    @Value("${aws.ses.smtp.port}")
    private int smtpPort;
    
    @Value("${aws.ses.smtp.username}")
    private String smtpUsername;
    
    @Value("${aws.ses.smtp.password}")
    private String smtpPassword;
    
    private Session mailSession;
    
    
    private  static final AmazonDynamoDB dynamodb = AmazonDynamoDBClientBuilder.standard().
            withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("https://dynamodb.us-east-1.amazonaws.com", "us-east-1")).
            withCredentials(new DefaultAWSCredentialsProviderChain()).
            build();
  
    @Bean
    public DynamoDBMapper dynamoDBMapper() {
        return new DynamoDBMapper(dynamodb);
    }
    
    public EmailService() {
        // Session will be initialized when first needed
    }
    
    private Session getMailSession() {
        if (mailSession == null) {
            Properties props = new Properties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.port", smtpPort);
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.connectiontimeout", "30000");
            props.put("mail.smtp.timeout", "30000");
            
            mailSession = Session.getDefaultInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUsername, smtpPassword);
                }
            });
        }
        return mailSession;
    }
    
    public void sendConfirmationEmail(Lead lead) {
        try {
            String subject = "Thank you for contacting us!";
            String htmlBody = buildConfirmationEmailHtml(lead);
            String textBody = buildConfirmationEmailText(lead);
            
            sendEmail(lead.getEmail(), subject, htmlBody, textBody);
            logger.info("Confirmation email sent to: {}", lead.getEmail());
            
        } catch (Exception e) {
            logger.error("Failed to send confirmation email to: {}", lead.getEmail(), e);
            throw new RuntimeException("Failed to send confirmation email", e);
        }
    }
    
    public void sendNotificationEmail(Lead lead) {
        try {
            String subject = "New Contact Form Submission";
            String htmlBody = buildNotificationEmailHtml(lead);
            String textBody = buildNotificationEmailText(lead);
            
            sendEmail(adminEmail, subject, htmlBody, textBody);
            logger.info("Notification email sent to admin: {}", adminEmail);
            
        } catch (Exception e) {
            logger.error("Failed to send notification email to admin", e);
            // Don't throw exception here as it's not critical for user experience
        }
    }
    
    private void sendEmail(String toEmail, String subject, String htmlBody, String textBody) {
        try {
            Session session = getMailSession();
            
            // Create message
            MimeMessage message = new MimeMessage(session);
            try {
				message.setFrom(new InternetAddress(fromEmail, "Rahul Ahuja"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(subject, "UTF-8");
            
            // Create multipart message for HTML and text
            Multipart multipart = new MimeMultipart("alternative");
            
            // Add text part
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(textBody, "text/plain; charset=UTF-8");
            multipart.addBodyPart(textPart);
            
            // Add HTML part
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody, "text/html; charset=UTF-8");
            multipart.addBodyPart(htmlPart);
            
            // Set the multipart message
            message.setContent(multipart);
            
            // Send the message
            Transport.send(message);
            logger.debug("Email sent successfully to: {}", toEmail);
            
        } catch (MessagingException e) {
            logger.error("Failed to send email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    private String buildConfirmationEmailHtml(Lead lead) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Thank you for contacting us</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #2c3e50;">Thank you for contacting !</h2>
                    
                    <p>Dear %s,</p>
                    
                    <p>Thank you for reaching out . I have received your message and will get back to you as soon as possible.</p>
                    
                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="margin-top: 0; color: #495057;">Your submission details:</h3>
                        <p><strong>Name:</strong> %s</p>
                        <p><strong>Email:</strong> %s</p>
                        <p><strong>Phone:</strong> %s</p>
                        <p><strong>Message:</strong><br>%s</p>
                    </div>
                    
                    <p>Expect Response within 24-48 hours during business days.</p>
                    
                    <p>Best regards,<br>Rahul Ahuja</p>
                    
                    <hr style="margin-top: 30px; border: none; border-top: 1px solid #dee2e6;">

                </div>
            </body>
            </html>
            """, 
            lead.getName(), lead.getName(), lead.getEmail(), lead.getPhone(), 
            lead.getDesc() != null ? lead.getDesc() : "No message provided");
    }
    
    private String buildConfirmationEmailText(Lead lead) {
        return String.format("""
            Thank you for contacting us!
            
            Dear %s,
            
            Thank you for reaching out . I have received your message and will get back to you as soon as possible.
            
            Your submission details:
            Name: %s
            Email: %s
            Phone: %s
            Message: %s
            
            Expect Response within 24-48 hours during business days.
            
            Best regards,
            Rahul Ahuja
            
            ---
            """, 
            lead.getName(), lead.getName(), lead.getEmail(), lead.getPhone(), 
            lead.getDesc() != null ? lead.getDesc() : "No message provided");
    }
    
    private String buildNotificationEmailHtml(Lead lead) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>New Contact Form Submission</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #dc3545;">New Contact Form Submission</h2>
                    
                    <p>You have received a new contact form submission.</p>
                    
                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="margin-top: 0; color: #495057;">Submission Details:</h3>
                        <p><strong>Name:</strong> %s</p>
                        <p><strong>Email:</strong> %s</p>
                        <p><strong>Phone:</strong> %s</p>
                        <p><strong>Message:</strong><br>%s</p>
                        <p><strong>Submitted:</strong> %s</p>
                    </div>
                    
                    <p>Please respond to this inquiry promptly.</p>
                </div>
            </body>
            </html>
            """, 
            lead.getName(), lead.getEmail(), lead.getPhone(), 
            lead.getDesc() != null ? lead.getDesc() : "No message provided",
            java.time.LocalDateTime.now().toString());
    }
    
    private String buildNotificationEmailText(Lead lead) {
        return String.format("""
            New Contact Form Submission
            
            You have received a new contact form submission.
            
            Submission Details:
            Name: %s
            Email: %s
            Phone: %s
            Message: %s
            Submitted: %s
            
            Please respond to this inquiry promptly.
            """, 
            lead.getName(), lead.getEmail(), lead.getPhone(), 
            lead.getDesc() != null ? lead.getDesc() : "No message provided",
            java.time.LocalDateTime.now().toString());
    }
    
    
    public void saveLead(LeadEntity employeeDTO)  {
    	
    	AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();

    	DynamoDBMapper mapper = new DynamoDBMapper(client);
       
        mapper.save(employeeDTO);
    }
    
}