package org.acme.mailer;

import java.io.Serializable;
import java.util.Map;

import com.badu.dto.emails.UpdateAccessEmailData;
import com.badu.services.emails.CustomerEmailService;

import io.smallrye.mutiny.Uni;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/mail")
public class MailResource {

  @Inject
  CustomerEmailService customerEmailService;

  @GET
  public Uni<Void> sendEmailUsingReactiveMailer() {
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
}
