package org.acme.hibernate.orm.panache;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.badu.entities.jobs.JobInstance;
import com.badu.entities.jobs.JobInstance.JobStatus;
import com.badu.services.jobs.JobManagerService;

@ApplicationScoped
public class FruitJobHandler {

  @Inject
  private JobManagerService jobManagerService;

  @Inject
  private FruitProcessingJob fruitProcessingJob;

  private static final Logger LOG = Logger.getLogger(FruitJobHandler.class);

  @Incoming("TEST_MSG")
  public final Uni<Void> processChatUsersUpdateRequest(final Fruit fruit) {
    LOG.error("Created new frout: " + fruit.name);

    JobInstance job = new JobInstance();
    job.name = "Fruit Processing";
    job.status = JobStatus.PENDING;

    return Panache.withTransaction(() -> job.<JobInstance>persist())
        .onItem().ifNotNull().call(newJob -> {
          return jobManagerService.submitJob(() -> {
            return fruitProcessingJob.execute(fruit, newJob.id);
          });
        })
        .onItem().ifNotNull().transformToUni(neeJob -> {
          return Uni.createFrom().voidItem();
        });
  }
}