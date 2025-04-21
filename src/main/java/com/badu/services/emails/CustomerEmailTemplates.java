package com.badu.services.emails;

import com.badu.dto.emails.UpdateAccessEmailData;

import io.quarkus.mailer.MailTemplate;
import io.quarkus.qute.CheckedTemplate;

@CheckedTemplate(basePath = "emails/customers", defaultName = CheckedTemplate.HYPHENATED_ELEMENT_NAME)
public class CustomerEmailTemplates {
  public static native MailTemplate.MailTemplateInstance chatAccessUpdated(UpdateAccessEmailData data);
}
