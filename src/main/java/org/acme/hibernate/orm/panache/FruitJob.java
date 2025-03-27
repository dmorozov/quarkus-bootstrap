package org.acme.hibernate.orm.panache;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FruitJob {

  private static final Logger LOG = Logger.getLogger(FruitJob.class);

  @Incoming("TEST_MSG")
  public final Uni<Void> processChatUsersUpdateRequest(final Fruit fruit) {
    LOG.error("Created new frout: " + fruit.name);
    return Uni.createFrom().voidItem();
  }
}