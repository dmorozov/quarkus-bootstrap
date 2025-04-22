package com.badu.services.emails;

import com.badu.dto.emails.UpdateAccessEmailData;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

public final class ReceiptTemplates {

  @CheckedTemplate(basePath = "receipts/customers", defaultName = CheckedTemplate.HYPHENATED_ELEMENT_NAME)
  static class Customer {

    static native TemplateInstance chatAccessUpdated(UpdateAccessEmailData data);
  }

  public static <T> TemplateInstance resolveTemplate(String receiptType, T data) {
    return switch (receiptType) {
      case "chatAccessUpdated" -> ReceiptTemplates.Customer.chatAccessUpdated((UpdateAccessEmailData) data);
      default -> throw new IllegalArgumentException("Unknown receipt type: " + receiptType);
    };
  }
}
