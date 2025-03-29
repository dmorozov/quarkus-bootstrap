package org.acme.hibernate.orm.panache;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.badu.dto.FruitJobRequest;
import com.badu.services.jobs.JobManagerService;

@ApplicationScoped
public class FruitJobHandler {

  @Inject
  private JobManagerService jobManagerService;

  @Inject
  private FruitProcessingJob fruitProcessingJob;

  private static final Logger LOG = Logger.getLogger(FruitJobHandler.class);

  @Incoming("TEST_MSG")
  public final Uni<Void> processChatUsersUpdateRequest(final FruitJobRequest jobRequest) {
    LOG.info("Handle new fruit CREATED: " + jobRequest.fruit.name);

    return jobManagerService.submitJob(() -> {
      LOG.info("Execute fruit job processing ...");
      return fruitProcessingJob.execute(jobRequest.fruit, jobRequest.jobId);
    });
  }
}