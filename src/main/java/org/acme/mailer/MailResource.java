package org.acme.mailer;

import com.badu.dto.emails.UpdateAccessEmailData;
import com.badu.services.emails.EmailService;

import io.smallrye.mutiny.Uni;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/mail")
public class MailResource {

  @Inject
  EmailService customerEmailService;

  @GET
  public Uni<Void> sendEmail() {
    UpdateAccessEmailData chatData = new UpdateAccessEmailData();
    chatData.chatName = "My Chat";
    chatData.isRevoked = false;
    chatData.role = "Admin";
    chatData.customerName = "John Doe";

    return customerEmailService.sendEmail(
        "chatAccessUpdated",
        "clement.escoffier@redhat.com",
        chatData);
  }

  @GET
  @Path("/attachment")
  public Uni<Void> sendEmailWithAttachment() {
    UpdateAccessEmailData chatData = new UpdateAccessEmailData();
    chatData.chatName = "My Chat";
    chatData.isRevoked = false;
    chatData.role = "Admin";
    chatData.customerName = "John Doe";

    return customerEmailService.sendEmailWithAttachment(
        "chatAccessUpdated", "chatAccessUpdated",
        "clement.escoffier@redhat.com",
        chatData);
  }
}
