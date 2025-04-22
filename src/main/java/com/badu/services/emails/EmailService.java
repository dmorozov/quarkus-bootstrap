package com.badu.services.emails;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EmailService {

  @Inject
  PdfReceiptService pdfReceiptService;

  public <T> Uni<Void> sendEmail(String emailType, String email, T data) {

    return EmailTemplates.resolveTemplate(emailType, data)
        .subject("Chat access updated")
        .to(email)
        .send();
  }

  public <T> Uni<Void> sendEmailWithAttachment(String emailType, String attachmentType, String email, T data) {
    return pdfReceiptService.generatePdfReceipt(attachmentType, data)
        .onItem()
        .transformToUni(pdfData -> {
          return EmailTemplates.resolveTemplate(emailType, data)
              .subject("Chat access updated")
              .to(email)
              .addAttachment(attachmentType + ".pdf", pdfData, "application/pdf")
              .send();
        });
  }
}
