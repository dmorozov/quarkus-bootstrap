package com.badu.services.jobs;

import java.util.UUID;
import java.util.function.Function;
import org.jboss.logging.Logger;
import com.badu.dto.IJobRequest;
import com.badu.dto.JobConfig;
import com.badu.entities.DataStatus;
import com.badu.entities.jobs.Job;
import com.badu.entities.jobs.JobExecutionState;
import com.badu.entities.jobs.JobExecutionType;
import com.badu.entities.jobs.JobState;
import com.badu.repositories.JobRespository;
import com.badu.utils.EntityUtils;
import com.badu.utils.TransactionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class JobService {

  @Inject
  private JobRespository jobRepository;

  @Inject
  private ObjectMapper objectMapper;

  private static final Logger LOG = Logger.getLogger(JobService.class);

  public final Uni<Void> scheduleOneTimeJob(final IJobRequest jobRequest) {
    return scheduleOneTimeJob(jobRequest,
        JobConfig.builder().build(),
        (jobId) -> Uni.createFrom().voidItem());
  }

  public final Uni<Void> scheduleOneTimeJob(final IJobRequest jobRequest,
      Function<UUID, Uni<Void>> onCommitCallback) {

    return scheduleOneTimeJob(jobRequest,
        JobConfig.builder().build(),
        (jobId) -> Uni.createFrom().voidItem());
  }

  public final Uni<Void> scheduleOneTimeJob(final IJobRequest jobRequest,
      final JobConfig jobConfig,
      Function<UUID, Uni<Void>> onCommitCallback) {

    return Panache.withTransaction(() -> {
      Job job = new Job();
      job.setName(jobRequest.getJobName());
      job.setType(jobRequest.getJobType());
      job.setExecutionType(JobExecutionType.ONETIME);
      job.setStatus(DataStatus.ACTIVE);
      job.setState(JobState.QUEUED);
      job.setMaxRetryCounter(jobConfig.getMaxRetryCounter());
      job.setRetryDelaySeconds(jobConfig.getRetryDelaySeconds());
      EntityUtils.updateUserContext(jobRequest.getTriggeredBy(), job);

      try {
        job.setParams(objectMapper.writeValueAsString(jobRequest));
      } catch (JsonProcessingException e) {
        return Uni.createFrom().failure(e);
      }

      return jobRepository.persist(job)
          .chain(jobExecution -> {
            TransactionUtils.registerPostTransactionCallback(() -> {
              onCommitCallback.apply(job.getId())
                  .subscribe()
                  .with(
                      result -> LOG.debug("Fruit job processing event sent."),
                      failure -> LOG.error("Unable to trigger Fruit job processing event.", failure));
            });

            return Uni.createFrom().voidItem();
          });
    });
  }
}
