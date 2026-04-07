package com.thechakra.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public void sendEmail(String to, String subject, String body) {
        // Fallback for any pure plain-text usages, though we'll route via HTML wrapper
        // now
        sendHtmlEmail(to, subject, buildCosmicHtml(subject, body));
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indicates HTML
            helper.setFrom(senderEmail);

            mailSender.send(message);
            log.info("Cosmic HTML Email sent to {}", to);
        } catch (MessagingException | MailException e) {
            log.error("Failed to send HTML email to {}. Error: {}", to, e.getMessage());
        }
    }

    private String buildCosmicHtml(String title, String message) {
        return "<div style=\"font-family: 'Inter', sans-serif; background-color: #0B0E14; color: #E2E8F0; padding: 40px; border-radius: 12px; border: 1px solid #1E293B; max-width: 600px; margin: 0 auto; text-align: center;\">"
                +
                "<h2 style=\"color: #60A5FA; letter-spacing: 2px; text-transform: uppercase; margin-bottom: 5px;\">The Chakra Protocol</h2>"
                +
                "<h4 style=\"color: #94A3B8; margin-top: 0; font-weight: 300; letter-spacing: 1px;\">" + title + "</h4>"
                +
                "<div style=\"background-color: #1E293B; padding: 30px; border-radius: 8px; margin: 30px 0; border: 1px solid rgba(96, 165, 250, 0.2);\">"
                +
                "<p style=\"color: #F8FAFC; font-size: 16px; line-height: 1.6; margin: 0;\">"
                + message.replace("\n", "<br/>") + "</p>" +
                "</div>" +
                "<div style=\"margin-top: 40px; border-top: 1px solid #1E293B; padding-top: 20px;\">" +
                "<p style=\"color: #475569; font-size: 12px; letter-spacing: 1px;\">© THE CHAKRA | SYSTEM TRANSMISSION</p>"
                +
                "</div>" +
                "</div>";
    }

    public void sendAdminApprovalRequest(String candidateEmail, String candidateName) {
        String subject = "AWAITING ROOT DIRECTIVE";
        String approvalLink = frontendUrl + "/root-verify?email=" + candidateEmail;
        String htmlBody = "<div style=\"font-family: 'Inter', sans-serif; background-color: #0B0E14; color: #E2E8F0; padding: 40px; border-radius: 12px; border: 1px solid #1E293B; max-width: 600px; margin: 0 auto; text-align: center;\">"
                +
                "<h2 style=\"color: #F87171; letter-spacing: 2px; text-transform: uppercase; margin-bottom: 5px;\">Authorization Required</h2>"
                +
                "<h4 style=\"color: #94A3B8; margin-top: 0; font-weight: 300;\">Admin Candidate: " + candidateName
                + "</h4>" +
                "<div style=\"background-color: #1E293B; padding: 30px; border-radius: 8px; margin: 30px 0; border: 1px solid rgba(248, 113, 113, 0.2);\">"
                +
                "<p style=\"color: #F8FAFC; font-size: 16px; margin-bottom: 20px;\">A new user has requested System Console access privileges. Only the Root Authority may generate their activation token.</p>"
                +
                "<a href=\"" + approvalLink
                + "\" style=\"display: inline-block; background-color: #2563EB; color: #FFFFFF; text-decoration: none; padding: 12px 24px; border-radius: 4px; font-weight: bold; letter-spacing: 1px; margin-top: 10px;\">INITIALIZE PROTOCOL</a>"
                +
                "</div>" +
                "<p style=\"color: #EF4444; font-size: 12px;\">⚠ RESTRICTED LINK. DO NOT FORWARD.</p>" +
                "<div style=\"margin-top: 40px; border-top: 1px solid #1E293B; padding-top: 20px;\">" +
                "<p style=\"color: #475569; font-size: 12px; letter-spacing: 1px;\">© THE CHAKRA | SYSTEM TRANSMISSION</p>"
                +
                "</div>" +
                "</div>";

        sendHtmlEmail("msrohith2007@gmail.com", subject, htmlBody);
    }

    public void sendMentorWelcomeEmail(String studentEmail, String studentName, String adminName) {
        String subject = "🎯 You've Been Assigned a Mentor on The Chakra!";

        String htmlBody = "<div style=\"font-family: 'Segoe UI', Arial, sans-serif; background-color: #0B0E14; color: #E2E8F0; padding: 40px 20px; max-width: 640px; margin: 0 auto; border-radius: 16px; border: 1px solid #1E293B;\">"

                // Header Banner
                + "<div style=\"background: linear-gradient(135deg, #06B6D4 0%, #8B5CF6 100%); border-radius: 12px; padding: 32px; text-align: center; margin-bottom: 32px;\">"
                + "<h1 style=\"color: white; font-size: 28px; font-weight: 900; margin: 0; letter-spacing: -0.5px;\">🚀 Your Mentor Has Arrived!</h1>"
                + "<p style=\"color: rgba(255,255,255,0.85); margin: 10px 0 0 0; font-size: 15px;\">Your career journey just got a personal co-pilot</p>"
                + "</div>"

                // Greeting
                + "<div style=\"padding: 0 16px;\">"
                + "<p style=\"font-size: 18px; font-weight: 600; color: #F1F5F9;\">Hey <span style=\"color:#22D3EE;\">"
                + studentName + "</span>! 👋</p>"
                + "<p style=\"color: #94A3B8; font-size: 15px; line-height: 1.7;\">Great news — <strong style=\"color: #F1F5F9;\">"
                + adminName
                + "</strong> has selected you as their mentee on <strong style=\"color: #22D3EE;\">The Chakra</strong> Career Platform. From today, you have a dedicated guide who will help you navigate your career path with clarity and confidence.</p>"

                // Motivation Box
                + "<div style=\"background: linear-gradient(135deg, #0F172A, #1E293B); border-left: 4px solid #22D3EE; border-radius: 10px; padding: 20px 24px; margin: 24px 0;\">"
                + "<p style=\"color: #22D3EE; font-size: 13px; font-weight: 700; text-transform: uppercase; letter-spacing: 1px; margin: 0 0 10px 0;\">💡 A Word From Your Mentor</p>"
                + "<p style=\"color: #CBD5E1; font-size: 15px; line-height: 1.7; margin: 0; font-style: italic;\">"
                + "\"Every expert was once a beginner. Every pro was once an amateur. The difference? They refused to quit. I believe in your potential, and I'm here to make sure you reach it. Let's build something great together.\"<br>"
                + "<span style=\"color:#94A3B8; font-size: 13px;\">— " + adminName + ", Your Mentor</span>"
                + "</p>"
                + "</div>"

                // What to Expect
                + "<p style=\"font-size: 16px; font-weight: 700; color: #F1F5F9; margin-top: 28px;\">📋 What You Can Expect:</p>"
                + "<ul style=\"color: #94A3B8; font-size: 14px; line-height: 2; padding-left: 20px;\">"
                + "<li>📅 <strong style=\"color:#F1F5F9;\">Daily Assignments</strong> — curated tasks tailored to your career path</li>"
                + "<li>📚 <strong style=\"color:#F1F5F9;\">Career Resources</strong> — hand-picked courses, roadmaps, and tools</li>"
                + "<li>🎯 <strong style=\"color:#F1F5F9;\">Personalized Guidance</strong> — advice specific to your strengths from your assessment</li>"
                + "<li>⚡ <strong style=\"color:#F1F5F9;\">Progress Tracking</strong> — your mentor will monitor your journey every step of the way</li>"
                + "</ul>"

                // Joke Section
                + "<div style=\"background: #1E293B; border-radius: 10px; padding: 20px; margin: 28px 0; text-align: center;\">"
                + "<p style=\"color: #FBBF24; font-size: 13px; font-weight: 700; margin: 0 0 8px 0;\">😄 A Little Humour to Start Your Journey:</p>"
                + "<p style=\"color: #CBD5E1; font-size: 14px; font-style: italic; margin: 0;\">\"Why do programmers prefer dark mode? Because light attracts bugs! 🐛\"<br><br>"
                + "But seriously — bugs in code are fixable. Bugs in your mindset aren't. So stay positive and keep debugging your thinking too! 😄</p>"
                + "</div>"

                // Motivational Quote
                + "<div style=\"background: linear-gradient(135deg, #7C3AED20, #06B6D420); border: 1px solid #7C3AED40; border-radius: 10px; padding: 20px 24px; margin: 24px 0; text-align: center;\">"
                + "<p style=\"color: #A78BFA; font-size: 20px; font-weight: 800; margin: 0 0 8px 0;\">\"The best time to plant a tree was 20 years ago.<br>The second best time is NOW. 🌱\"</p>"
                + "<p style=\"color: #64748B; font-size: 12px; margin: 0;\">— Ancient Proverb (still very accurate for career planning)</p>"
                + "</div>"

                // CTA Button
                + "<div style=\"text-align: center; margin: 32px 0;\">"
                + "<a href=\"" + frontendUrl + "/student\" style=\"display: inline-block; background: linear-gradient(135deg, #06B6D4, #8B5CF6); color: white; font-weight: 800; font-size: 16px; padding: 14px 36px; border-radius: 50px; text-decoration: none; letter-spacing: 0.5px;\">🎯 Open My Dashboard</a>"
                + "</div>"

                // Footer
                + "<div style=\"border-top: 1px solid #1E293B; padding-top: 20px; margin-top: 20px; text-align: center;\">"
                + "<p style=\"color: #334155; font-size: 12px; margin: 0;\">© THE CHAKRA Career Assessment Platform | Built for FSAD-PS30</p>"
                + "<p style=\"color: #334155; font-size: 11px; margin: 4px 0 0 0;\">This email was sent to <span style=\"color:#475569;\">"
                + studentEmail + "</span></p>"
                + "</div>"
                + "</div>"
                + "</div>";

        sendHtmlEmail(studentEmail, subject, htmlBody);
    }
}
