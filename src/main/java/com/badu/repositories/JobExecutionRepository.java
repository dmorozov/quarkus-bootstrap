package com.badu.repositories;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

import org.jboss.logging.Logger;

import com.badu.entities.jobs.JobExecution;
import com.badu.entities.jobs.JobExecutionLog;
import com.badu.entities.jobs.JobExecutionState;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
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
        return findById(jobExecutionId)
            .onItem().ifNotNull().transformToUni(job -> {
              LOG.info("Job is COMPLETED");
              job.setState(JobExecutionState.COMPLETED);
              return persist(job);
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
          return findById(jobExecutionId)
              .onItem().ifNotNull().transformToUni(job -> {
                LOG.info("Job is FAILED");
                job.setState(JobExecutionState.FAILED);
                return persist(job);
              });
        });
  }

  public Uni<JobExecution> findMostRecentByJobId(UUID id) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'findByJobId'");
  }
}
