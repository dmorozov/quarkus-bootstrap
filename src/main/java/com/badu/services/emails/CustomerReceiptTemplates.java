package com.badu.services.emails;

import com.badu.dto.emails.UpdateAccessEmailData;

import io.quarkus.mailer.MailTemplate;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;

@CheckedTemplate(basePath = "receipts", defaultName = CheckedTemplate.HYPHENATED_ELEMENT_NAME)
public class CustomerReceiptTemplates {
  public static native MailTemplate.MailTemplateInstance chatAccessUpdated(UpdateAccessEmailData data);

  @Location("receipts/chat-access-updated.html")
  Template report;
}
