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
import io.smallrye.mutiny.tuples.Tuple2;
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

  protected final Class<T> paramsType;

  protected abstract Uni<Void> processJob(final JobExecution jobExecution, final T jobRequest);

  @SuppressWarnings("unchecked")
  protected AbstractJobHandler() {
    // Dynamically resolve generic type which defines the type of job parameters
    Class<?> currentClass = getClass();
    while (currentClass.getSuperclass() != AbstractJobHandler.class) {
      currentClass = currentClass.getSuperclass();
      if (currentClass == null) {
        throw new IllegalStateException(
            "Could not determine type arguments for " + getClass().getName());
      }
    }
    this.paramsType = (Class<T>) ((ParameterizedType) currentClass.getGenericSuperclass())
        .getActualTypeArguments()[0];
  }

  /**
   * Another way to lock and process job
   * <code>
   public Uni<JobRecord> findAndLockJob() {
        return jobRepository.withTransaction(tx -> {
            String query = "FROM JobRecord WHERE processed = false ORDER BY createdAt ASC";
            return jobRepository.getSession()
                .createQuery(query, JobRecord.class)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .setMaxResults(1)
                .getSingleResultOrNull()
                .onItem().ifNotNull().invoke(job -> {
                    job.setProcessed(true);
                    job.setProcessedAt(Instant.now());
                    // No need to explicitly persist as it's a managed entity
                });
        });
    }
   * </code>
   */

  protected static boolean isJobProcessedOrInProgress(final Job job, final JobExecution jobExecution) {
    boolean isProcessed = job.isProcessed();
    if (!isProcessed && jobExecution != null) {

      ZonedDateTime now = ZonedDateTime.now();
      ZonedDateTime expectedRetryTime = jobExecution.getStartTime().plusSeconds(job.getRetryDelaySeconds());

      // it is either completed or still in progress
      if (jobExecution.getState() == JobExecutionState.COMPLETED || expectedRetryTime.isAfter(now)) {
        return true;
      }
    }

    return isProcessed;
  }

  protected Uni<Void> lockAndProcessJob(final UUID jobId, final T jobRequest) {

    return Panache.withTransaction(() -> {
      // Get and LOCK job to prevent the concurrent processing
      return jobRespository.findById(jobId, LockModeType.PESSIMISTIC_WRITE)
          .onItem().ifNotNull().transformToUni(lockedJob -> {
            return jobExecutionRepository.findMostRecentByJobId(lockedJob.getId())
                .onItem().transformToUni(jobExecution -> {
                  boolean isProcessedOrInProgress = isJobProcessedOrInProgress(lockedJob, jobExecution);

                  if (isProcessedOrInProgress) {
                    LOG.info("Skipping job processing. Job with id " + jobId + " is either completed or in progress.");
                    return Uni.createFrom().nullItem();
                  }

                  try {
                    // Parse job params
                    final T params = objectMapper.readValue(lockedJob.getParams(), paramsType);
                    if (jobRequest != null) {
                      ObjectMerger.merge(params, jobRequest);
                    }

                    int retryCounter = jobExecution != null ? jobExecution.getRetryCounter() + 1 : 1;
                    return createJobExecution(lockedJob, params, retryCounter)
                        .onItem().ifNotNull().transform(execution -> {
                          return Tuple2.of(execution, params);
                        });
                  } catch (JacksonException e) {
                    return Uni.createFrom().failure(e);
                  }
                });
          })
          .onItem().ifNull().switchTo(() -> {
            LOG.info("Skipping job processing. Job with id " + jobId + " not found");
            return Uni.createFrom().nullItem();
          });
    })
        .onItem().ifNotNull().transformToUni(newExecution -> {
          return jobManagerService.submitJob(() -> {
            LOG.info("Execute '" + newExecution.getItem1().getName() + "' job processing ...");
            return processJob(newExecution.getItem1(), newExecution.getItem2());
          });
        })
        .onItem().ifNull().switchTo(() -> Uni.createFrom().voidItem());
  }

  private Uni<JobExecution> createJobExecution(final Job lockedJob, final T params, final int retryCounter)
      throws JsonProcessingException {
    JobExecution jobExecution = new JobExecution();
    jobExecution.setJobId(lockedJob.getId());
    jobExecution.setName(lockedJob.getName());
    jobExecution.setStartTime(ZonedDateTime.now());
    jobExecution.setState(JobExecutionState.QUEUED);
    jobExecution.setParams(objectMapper.writeValueAsString(params));
    jobExecution.setTriggeredBy(params.getTriggeredBy());
    jobExecution.setRetryCounter(retryCounter);
    jobExecution.setProgress(0);
    return jobExecutionRepository.persistAndFlush(jobExecution);
  }
}
