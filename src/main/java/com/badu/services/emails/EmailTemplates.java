package com.badu.services.emails;

import com.badu.dto.emails.UpdateAccessEmailData;

import io.quarkus.mailer.MailTemplate;
import io.quarkus.mailer.MailTemplate.MailTemplateInstance;
import io.quarkus.qute.CheckedTemplate;

public class EmailTemplates {

  @CheckedTemplate(basePath = "emails/customers", defaultName = CheckedTemplate.HYPHENATED_ELEMENT_NAME)
  static class Customer {

    static native MailTemplate.MailTemplateInstance chatAccessUpdated(UpdateAccessEmailData data);
  }

  public static <T> MailTemplateInstance resolveTemplate(String emailType, T data) {
    return switch (emailType) {
      case "chatAccessUpdated" -> EmailTemplates.Customer.chatAccessUpdated((UpdateAccessEmailData) data);
      default -> throw new IllegalArgumentException("Unknown email type: " + emailType);
    };
  }
}
