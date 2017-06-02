/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.business.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author adamc
 */
public class EmailUtil {
    private static final Logger LOGGER = Logger.getLogger(EmailUtil.class.getName());
    
    private static Session mailSession;
    
    private EmailUtil() {
        // not public
    }
    
    static {
        try {
            mailSession = (Session) new InitialContext().lookup("mail/jlab");
        } catch (NamingException e) {
            LOGGER.log(Level.WARNING, "Mail resource lookup failed", e);
            throw new ExceptionInInitializerError(e);
        }
    }
    
    public static void sendEmail(String[] to, String from, String subject, String body) {
        String jlabDomain = "@jlab.org";
        try {
            Address fromAddress = new InternetAddress(from + jlabDomain);
            LOGGER.log(Level.FINEST, "SendEmail: {0}{1}", new Object[]{from, jlabDomain});
            if (subject == null || subject.isEmpty()) {
                throw new IllegalArgumentException("subject must not be empty");
            }

            if (body == null || body.isEmpty()) {
                throw new IllegalArgumentException("message must not be empty");
            }

            subject = "RFDashboard Feedback: " + subject;
            Address[] toAddresses = new Address[to.length];
            for ( int i = 0; i < to.length; i++) {
                toAddresses[i] = new InternetAddress(to[i] + jlabDomain);
            }

            MimeMessage message = new MimeMessage(mailSession);
            message.setFrom(fromAddress);
            message.setRecipients(Message.RecipientType.TO, toAddresses);
            message.setSubject(subject);
            message.setText(body, "UTF-8");
            message.saveChanges();
            
            Transport tr = mailSession.getTransport();
            tr.connect();
            tr.sendMessage(message, message.getAllRecipients());
            tr.close();
            
        } catch (AddressException e) {
            throw new IllegalArgumentException("Invalid address", e);
        } catch (MessagingException e) {
            throw new IllegalArgumentException("Unable to send email", e);
        }
    }
}
