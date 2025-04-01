package com.badu.repositories;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.jboss.logging.Logger;

import com.badu.entities.jobs.JobExecution;
import com.badu.entities.jobs.JobExecutionLog;
import com.badu.entities.jobs.JobExecution.JobStatus;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class JobExecutionRepository implements PanacheRepository<JobExecution> {

  private static final Logger LOG = Logger.getLogger(JobExecutionRepository.class);

  @Inject
  private JobExecutionLogRepository logRepository;

  public Uni<JobExecutionLog> recordJobSuccess(final Long jobId, final String action, final int progress) {

    LOG.info("Record job success: " + action + ", progress: " + progress);

    JobExecutionLog log = new JobExecutionLog();
    log.jobExecutionId = jobId;
    log.action = action;
    log.progress = progress;

    Uni<JobExecutionLog> rez = logRepository.persist(log);
    if (progress >= 100) {
      rez = rez.onItem().call(newLog -> {
        return findById(jobId)
            .onItem().ifNotNull().transformToUni(job -> {
              LOG.info("Job is SUCCEEDED");
              job.status = JobStatus.SUCCEEDED;
              return persist(job);
            });
      });
    }

    return rez;
  }

  public Uni<JobExecutionLog> recordJobFailure(final Long jobId, final String action, final Throwable e) {

    LOG.info("Record job failure: " + action + ", error: " + e.getMessage());

    JobExecutionLog log = new JobExecutionLog();
    log.jobExecutionId = jobId;
    log.action = action;
    if (e != null) {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      String errorDetails = sw.toString();
      log.errorDetails = errorDetails.length() > 2048
          ? errorDetails.substring(0, 2048 - 1)
          : errorDetails;
    }
    return logRepository.persist(log)
        .onItem().call(logItem -> {
          return findById(jobId)
              .onItem().ifNotNull().transformToUni(job -> {
                LOG.info("Job is FAILED");
                job.status = JobStatus.FAILED;
                return persist(job);
              });
        });
  }
}
