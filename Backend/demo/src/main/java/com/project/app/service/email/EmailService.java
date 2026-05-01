package com.project.app.service.email;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendBookingConfirmation(
            String fromEmail,      // email отеля из hotel_contacts
            String fromName,       // название отеля
            String toEmail,        // email гостя
            String guestName,
            Long bookingId,
            String hotelName,
            String roomTypeName,
            String roomNumber,
            LocalDate checkIn,
            LocalDate checkOut,
            String totalPrice,
            String currency
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);   // от имени отеля
            helper.setTo(toEmail);
            helper.setSubject("✅ Booking Confirmed #" + bookingId + " — " + hotelName);
            helper.setText(buildHtml(
                    guestName, bookingId, hotelName,
                    roomTypeName, roomNumber,
                    checkIn, checkOut, totalPrice, currency
            ), true);

            mailSender.send(message);
            System.out.println("✅ Email sent to " + toEmail + " from " + fromEmail);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Failed to send email: " + e.getMessage());
        }
    }

    private String buildHtml(
            String guestName, Long bookingId, String hotelName,
            String roomTypeName, String roomNumber,
            LocalDate checkIn, LocalDate checkOut,
            String totalPrice, String currency
    ) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");

        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;padding:0;background:#f7f5f1;font-family:'Segoe UI',Arial,sans-serif;">
              <div style="max-width:600px;margin:40px auto;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,.08);">

                <div style="background:linear-gradient(135deg,#0f1923,#0d3b6e);padding:40px;text-align:center;">
                  <h1 style="margin:0;color:#fff;font-size:26px;font-weight:800;">%s</h1>
                  <p style="margin:8px 0 0;color:rgba(255,255,255,.5);font-size:13px;">Booking Confirmation</p>
                </div>

                <div style="background:#f0fdf4;border-bottom:1px solid #bbf7d0;padding:24px 40px;text-align:center;">
                  <div style="font-size:44px;margin-bottom:8px;">✅</div>
                  <h2 style="margin:0;color:#0b7a4e;font-size:22px;font-weight:700;">Booking Confirmed!</h2>
                  <p style="margin:8px 0 0;color:#4b5563;font-size:14px;">Dear %s, your reservation has been successfully confirmed.</p>
                </div>

                <div style="padding:32px 40px;">
                  <div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;padding:24px;margin-bottom:24px;">
                    <div style="font-size:11px;font-weight:700;color:#6b7a8d;text-transform:uppercase;letter-spacing:1px;margin-bottom:16px;">Booking Details</div>
                    <table style="width:100%%;border-collapse:collapse;">
                      <tr>
                        <td style="padding:9px 0;color:#6b7a8d;font-size:13px;width:40%%;">Booking ID</td>
                        <td style="padding:9px 0;font-weight:800;color:#0d3b6e;font-family:monospace;font-size:15px;">#%d</td>
                      </tr>
                      <tr style="border-top:1px solid #e2e8f0;">
                        <td style="padding:9px 0;color:#6b7a8d;font-size:13px;">Hotel</td>
                        <td style="padding:9px 0;font-weight:600;font-size:14px;">%s</td>
                      </tr>
                      <tr style="border-top:1px solid #e2e8f0;">
                        <td style="padding:9px 0;color:#6b7a8d;font-size:13px;">Room</td>
                        <td style="padding:9px 0;font-weight:600;font-size:14px;">%s · Room #%s</td>
                      </tr>
                      <tr style="border-top:1px solid #e2e8f0;">
                        <td style="padding:9px 0;color:#6b7a8d;font-size:13px;">Check-in</td>
                        <td style="padding:9px 0;font-weight:600;font-size:14px;">%s</td>
                      </tr>
                      <tr style="border-top:1px solid #e2e8f0;">
                        <td style="padding:9px 0;color:#6b7a8d;font-size:13px;">Check-out</td>
                        <td style="padding:9px 0;font-weight:600;font-size:14px;">%s</td>
                      </tr>
                      <tr style="border-top:2px solid #0d3b6e;">
                        <td style="padding:14px 0;color:#0d3b6e;font-weight:700;font-size:15px;">Total</td>
                        <td style="padding:14px 0;font-weight:800;font-size:20px;color:#0d3b6e;">%s %s</td>
                      </tr>
                    </table>
                  </div>

                  <div style="text-align:center;margin-bottom:28px;">
                    <span style="background:#f0fdf4;color:#0b7a4e;border:1px solid #bbf7d0;border-radius:20px;padding:6px 18px;font-size:12px;font-weight:700;letter-spacing:.5px;">
                      STATUS: CONFIRMED
                    </span>
                  </div>

                  <p style="color:#6b7a8d;font-size:13px;line-height:1.8;text-align:center;">
                    Thank you for choosing %s.<br>
                    We look forward to welcoming you!
                  </p>
                </div>

                <div style="background:#f8fafc;border-top:1px solid #e2e8f0;padding:20px 40px;text-align:center;">
                  <p style="margin:0;color:#94a3b8;font-size:12px;">This email was sent by %s · Hotel Management System</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                hotelName,
                guestName,
                bookingId,
                hotelName,
                roomTypeName, roomNumber,
                checkIn.format(fmt),
                checkOut.format(fmt),
                currency, totalPrice,
                hotelName,
                hotelName
        );
    }
}