package com.badu.services.emails;

import com.badu.dto.emails.UpdateAccessEmailData;

import io.quarkus.mailer.MailTemplate.MailTemplateInstance;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EmailService {

  @Inject
  PdfReceiptService pdfReceiptService;

  public <T> Uni<Void> sendEmail(String emailType, String email, T data) {

    return resolveTemplate(emailType, data)
        .subject("Chat access updated")
        .to(email)
        .send();
  }

  public <T> Uni<Void> sendEmailWithAttachment(String emailType, String attachmentType, String email, T data) {
    return pdfReceiptService.generatePdfReceipt(attachmentType, data)
        .onItem()
        .transformToUni(pdfData -> {
          return resolveTemplate(emailType, data)
              .subject("Chat access updated")
              .to(email)
              .addAttachment(attachmentType + ".pdf", pdfData, "application/pdf")
              .send();
        });
  }

  private <T> MailTemplateInstance resolveTemplate(String emailType, T data) {
    return switch (emailType) {
      case "chatAccessUpdated" -> CustomerEmailTemplates.chatAccessUpdated((UpdateAccessEmailData) data);
      default -> throw new IllegalArgumentException("Unknown email type: " + emailType);
    };
  }
}
