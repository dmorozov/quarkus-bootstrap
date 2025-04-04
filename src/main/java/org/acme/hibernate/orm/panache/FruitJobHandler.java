package org.acme.hibernate.orm.panache;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.badu.dto.FruitJobRequest;
import com.badu.entities.jobs.AbstractJobHandler;
import com.badu.entities.jobs.JobExecution;

@ApplicationScoped
public class FruitJobHandler extends AbstractJobHandler<FruitJobRequest> {

  @Inject
  private FruitProcessingJob fruitProcessingJob;

  private static final Logger LOG = Logger.getLogger(FruitJobHandler.class);

  @Incoming(FruitJobRequest.JOB_TYPE)
  public final Uni<Void> processChatUsersUpdateRequest(final FruitJobRequest jobRequest) {
    LOG.info("Handle new fruit CREATED: " + jobRequest.fruit.name);
    return lockAndProcessJob(jobRequest.getJobId(), jobRequest);
  }

  @Scheduled(every = "10s")
  void scheduleJobProcessing() {
    // TODO: Implement retry job processing

    // 1. Query Job where Job.type == FruitJobRequest.JOB_TYPE
    // 2. Check if Job does not have JobExecution or it is created more then 10s ago
    // and not completed. Check Job.retryDelaySeconds
    // 3. Call lockAndProcessJob for processing
    LOG.debug("Pending job check is not implemented!");
  }

  protected Uni<Void> processJob(final JobExecution jobExecution, final FruitJobRequest jobRequest) {
    return fruitProcessingJob.execute(jobRequest.fruit, jobExecution.getId());
  }
}