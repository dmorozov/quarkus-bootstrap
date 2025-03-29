package com.badu.repositories;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.jboss.logging.Logger;

import com.badu.entities.jobs.JobInstance;
import com.badu.entities.jobs.JobLog;
import com.badu.entities.jobs.JobInstance.JobStatus;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JobRepository implements PanacheRepository<JobInstance> {

  private static final Logger LOG = Logger.getLogger(JobRepository.class);

  public Uni<JobLog> recordJobSuccess(final Long jobId, final String action, final int progress) {

    LOG.info("Record job success: " + action + ", progress: " + progress);

    JobLog log = new JobLog();
    log.jobId = jobId;
    log.action = action;
    log.progress = progress;

    Uni<JobLog> rez = log.persist();
    if (progress >= 100) {
      rez = rez.onItem().call(newLog -> {
        return JobInstance.<JobInstance>findById(jobId)
            .onItem().ifNotNull().transformToUni(job -> {
              LOG.info("Job is SUCCEEDED");
              job.status = JobStatus.SUCCEEDED;
              return job.persist();
            });
      });
    }

    return rez;
  }

  public Uni<JobLog> recordJobFailure(final Long jobId, final String action, final Throwable e) {

    LOG.info("Record job failure: " + action + ", error: " + e.getMessage());

    JobLog log = new JobLog();
    log.jobId = jobId;
    log.action = action;
    if (e != null) {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      String errorDetails = sw.toString();
      log.errorDetails = errorDetails.length() > 2048
          ? errorDetails.substring(0, 2048 - 1)
          : errorDetails;
    }
    return log.<JobLog>persist()
        .onItem().call(logItem -> {
          return JobInstance.<JobInstance>findById(jobId)
              .onItem().ifNotNull().transformToUni(job -> {
                LOG.info("Job is FAILED");
                job.status = JobStatus.FAILED;
                return job.persist();
              });
        });
  }
}
