package org.acme.hibernate.orm.panache;

import java.util.UUID;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.badu.dto.FruitJobRequest;
import com.badu.entities.jobs.Job;
import com.badu.repositories.JobRespository;
import com.badu.services.jobs.JobManagerService;
import com.badu.utils.ObjectMerger;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class FruitJobHandler {

  @Inject
  private JobManagerService jobManagerService;

  @Inject
  private FruitProcessingJob fruitProcessingJob;

  @Inject
  private JobRespository jobRespository;

  @Inject
  private ObjectMapper objectMapper;

  private static final Logger LOG = Logger.getLogger(FruitJobHandler.class);

  @Incoming(FruitJobRequest.JOB_TYPE)
  public final Uni<Void> processChatUsersUpdateRequest(final FruitJobRequest jobRequest) {
    LOG.info("Handle new fruit CREATED: " + jobRequest.fruit.name);
    return processJob(jobRequest.getJobId(), jobRequest);
  }

  @Scheduled(every = "10s")
  void scheduleJobProcessing() {
    // TODO: Query Job where Job.type == FruitJobRequest.JOB_TYPE
    // 1. Job should not have JobExecution or it is created more then 10s ago and
    // not started.
    // 2. Load job from the DB and deserialize Job.params as FruitJobRequest
    LOG.debug("Pending job check is not implemented!");
  }

  protected Uni<Tuple2<Job, FruitJobRequest>> resolveParams(final UUID jobId, final FruitJobRequest jobRequest) {
    jobRespository.findById(jobId)
        .onItem().ifNotNull().transformToUni(job -> {
          try {
            final FruitJobRequest params = objectMapper.readValue(job.getParams(), FruitJobRequest.class);
            if (jobRequest != null) {
              ObjectMerger.merge(params, jobRequest);
            }
            return params;
          } catch (JacksonException e) {

          }
        });
    // todo: ifNull
    // todo: if failed
  }

  protected Uni<Void> processJob(final UUID jobId, final FruitJobRequest jobRequest) {
    // TODO: try to lock job by id. If successfult -> process

    return jobManagerService.submitJob(() -> {
      LOG.info("Execute fruit job processing ...");
      return fruitProcessingJob.execute(jobRequest.fruit, jobRequest.jobId);
    });
  }
}