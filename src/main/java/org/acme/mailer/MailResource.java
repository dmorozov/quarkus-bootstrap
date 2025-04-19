package org.acme.mailer;

import java.util.List;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/mail")
public class MailResource {

  @Inject
  Mailer mailer;

  @GET
  @Blocking
  public void sendEmail() {
    mailer.send(Mail.withText("your-destination-email@quarkus.io", "Ahoy from Quarkus",
        "A simple email sent from a Quarkus application."));
  }

  @GET
  @Path("/test")
  @Blocking
  public String sendTestEmail() {
    Mail m = new Mail();
    m.setFrom("admin@hallofjustice.net");
    m.setTo(List.of("superheroes@quarkus.io"));
    m.setSubject("WARNING: Super Villain Alert");
    m.setText("Lex Luthor has been seen in Gotham City!");
    mailer.send(m);

    return "Sent";
  }

  @Inject
  ReactiveMailer reactiveMailer;

  @GET
  @Path("/reactive")
  public Uni<Void> sendEmailUsingReactiveMailer() {
    return reactiveMailer.send( // <4>
        Mail.withText("clement.escoffier@redhat.com",
            "Ahoy from Quarkus",
            "A simple email sent from a Quarkus application using the reactive API."));
  }

}
