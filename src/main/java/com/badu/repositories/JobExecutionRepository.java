package com.badu.repositories;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

import org.jboss.logging.Logger;

import com.badu.entities.jobs.JobExecution;
import com.badu.entities.jobs.JobExecutionLog;
import com.badu.entities.jobs.JobExecutionState;
import com.badu.entities.jobs.JobState;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class JobExecutionRepository implements PanacheRepositoryBase<JobExecution, UUID> {

  private static final Logger LOG = Logger.getLogger(JobExecutionRepository.class);

  @Inject
  private JobExecutionLogRepository logRepository;

  public Uni<JobExecutionLog> recordJobSuccess(final UUID jobExecutionId, final String action, final int progress) {

    LOG.info("Record job success: " + action + ", progress: " + progress);

    JobExecutionLog log = new JobExecutionLog();
    log.setJobExecutionId(jobExecutionId);
    log.setAction(action);
    log.setProgress(progress);

    Uni<JobExecutionLog> rez = logRepository.persist(log);
    if (progress >= 100) {
      rez = rez.onItem().call(newLog -> {
        return find("from JobExecution p left join fetch p.job where p.id = :jobExecutionId", jobExecutionId)
            .singleResult()
            .onItem().ifNotNull().transformToUni(jobExecution -> {
              LOG.info("Job is COMPLETED");
              jobExecution.setState(JobExecutionState.COMPLETED);
              jobExecution.getJob().setState(JobState.COMPLETED);
              return persist(jobExecution);
            });
      });
    }

    return rez;
  }

  public Uni<JobExecutionLog> recordJobFailure(final UUID jobExecutionId, final String action, final Throwable e) {

    LOG.info("Record job failure: " + action + ", error: " + e.getMessage());

    JobExecutionLog log = new JobExecutionLog();
    log.setJobExecutionId(jobExecutionId);
    log.setAction(action);
    if (e != null) {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      String errorDetails = sw.toString();
      log.setErrorDetails(errorDetails.length() > 2048
          ? errorDetails.substring(0, 2048 - 1)
          : errorDetails);
    }
    return logRepository.persist(log)
        .onItem().call(logItem -> {
          return find("from JobExecution p left join fetch p.job where p.id = :jobExecutionId", jobExecutionId)
              .singleResult()
              .onItem().ifNotNull().transformToUni(jobExecution -> {
                LOG.info("Job is FAILED");
                jobExecution.setState(JobExecutionState.FAILED);
                if (jobExecution.getRetryCounter() >= jobExecution.getJob().getMaxRetryCounter()) {
                  jobExecution.getJob().setState(JobState.FAILED);
                }
                return persist(jobExecution);
              });
        });
  }

  public Uni<JobExecution> findMostRecentByJobId(UUID jobId) {
    return find("from JobExecution p where p.jobId = :jobId",
        Sort.by("p.startTime", Sort.Direction.Descending), jobId)
        .singleResult();
  }
}
