package com.badu.services.emails;

import com.badu.dto.emails.UpdateAccessEmailData;

import io.quarkus.mailer.MailTemplate.MailTemplateInstance;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CustomerEmailService {

  public <T> Uni<Void> sendEmail(String emailType, String email, T data) {

    return resolveTemplate(emailType, data)
        .subject("Chat access updated")
        .to(email)
        .send();
  }

  private <T> MailTemplateInstance resolveTemplate(String emailType, T data) {
    return switch (emailType) {
      case "chatAccessUpdated" -> CustomerEmailTemplates.chatAccessUpdated((UpdateAccessEmailData) data);
      default -> throw new IllegalArgumentException("Unknown email type: " + emailType);
    };
  }
}
