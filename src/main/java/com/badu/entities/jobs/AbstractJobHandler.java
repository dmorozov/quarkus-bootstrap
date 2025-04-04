package com.badu.entities.jobs;

import java.lang.reflect.ParameterizedType;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.jboss.logging.Logger;
import com.badu.dto.IJobRequest;
import com.badu.repositories.JobExecutionRepository;
import com.badu.repositories.JobRespository;
import com.badu.services.jobs.JobManagerService;
import com.badu.utils.ObjectMerger;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;

public abstract class AbstractJobHandler<T extends IJobRequest> {

  private static final Logger LOG = Logger.getLogger(AbstractJobHandler.class);

  @Inject
  private JobManagerService jobManagerService;

  @Inject
  private JobRespository jobRespository;

  @Inject
  private JobExecutionRepository jobExecutionRepository;

  @Inject
  private ObjectMapper objectMapper;

  protected final Class<T> type;

  @SuppressWarnings("unchecked")
  protected AbstractJobHandler() {
    Class<?> currentClass = getClass();
    while (currentClass.getSuperclass() != AbstractJobHandler.class) {
      currentClass = currentClass.getSuperclass();
      if (currentClass == null) {
        throw new IllegalStateException(
            "Could not determine type arguments for " + getClass().getName());
      }
    }
    this.type = (Class<T>) ((ParameterizedType) currentClass.getGenericSuperclass())
        .getActualTypeArguments()[0];
  }

  protected abstract Uni<Void> processJob(final JobExecution jobExecution, final T jobRequest);

  protected Uni<Void> lockAndProcessJob(final UUID jobId, final T jobRequest) {

    return Panache.withTransaction(() -> {
      // Get and LOCK job to prevent the concurrent processing
      return jobRespository.findById(jobId, LockModeType.PESSIMISTIC_WRITE)
          .onItem().ifNotNull().transformToUni(lockedJob -> {
            return isJobProcessedOrInProgress(lockedJob)
                .onItem().transformToUni(isProcessedOrInProgress -> {
                  if (isProcessedOrInProgress) {
                    LOG.info("Skipping job processing. Job with id " + jobId + " is already handled.");
                    return Uni.createFrom().voidItem();
                  }

                  try {
                    // Parse job params
                    final T params = objectMapper.readValue(lockedJob.getParams(), type);
                    if (jobRequest != null) {
                      ObjectMerger.merge(params, jobRequest);
                    }

                    return createJobExecution(lockedJob, params)
                        .onItem().transformToUni(jobExecution -> {
                          return jobManagerService.submitJob(() -> {
                            LOG.info("Execute fruit job processing ...");
                            return processJob(jobExecution, params);
                          });
                        });
                  } catch (JacksonException e) {
                    return Uni.createFrom().failure(e);
                  }
                });
          })
          .onItem().ifNull().switchTo(() -> {
            LOG.info("Skipping job processing. Job with id " + jobId + " not found");
            return Uni.createFrom().voidItem();
          });
    });
  }

  private Uni<JobExecution> createJobExecution(final Job lockedJob, final T params) throws JsonProcessingException {
    JobExecution jobExecution = new JobExecution();
    jobExecution.setJobId(lockedJob.getId());
    jobExecution.setStartTime(ZonedDateTime.now());
    jobExecution.setState(JobExecutionState.QUEUED);
    jobExecution.setParams(objectMapper.writeValueAsString(params));
    jobExecution.setTriggeredBy(params.getTriggeredBy());
    return jobExecutionRepository.persistAndFlush(jobExecution);
  }

  private Uni<Boolean> isJobProcessedOrInProgress(final Job lockedJob) {
    if (lockedJob.isProcessed()) {
      return Uni.createFrom().item(true);
    }

    return isJobExecutionInProgress(lockedJob);
  }

  private Uni<Boolean> isJobExecutionInProgress(final Job lockedJob) {
    return jobExecutionRepository.findMostRecentByJobId(lockedJob.getId())
        .onItem().ifNotNull().transformToUni(jobExecution -> {
          return Uni.createFrom().item(
              jobExecution.getStartTime().plusSeconds(lockedJob.getRetryDelaySeconds()).isAfter(ZonedDateTime.now()));
        })
        .onItem().ifNull().switchTo(() -> {
          return Uni.createFrom().item(false);
        });
  }
}
