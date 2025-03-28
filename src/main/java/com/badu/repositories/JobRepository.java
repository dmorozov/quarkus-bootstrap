package com.badu.repositories;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.badu.entities.jobs.JobInstance;
import com.badu.entities.jobs.JobLog;
import com.badu.entities.jobs.JobInstance.JobStatus;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JobRepository implements PanacheRepository<JobInstance> {

  public Uni<JobLog> recordJobSuccess(final Long jobId, final String action, final int progress) {
    JobLog log = new JobLog();
    log.jobId = jobId;
    log.action = action;
    log.progress = progress;

    Uni<JobLog> rez = log.persist();
    if (progress >= 100) {
      rez = rez.onItem().call(newLog -> {
        return JobInstance.<JobInstance>findById(jobId)
            .onItem().ifNotNull().transformToUni(job -> {
              job.status = JobStatus.SUCCEEDED;
              return job.persist();
            });
      });
    }

    return rez;
  }

  public Uni<JobLog> recordJobFailure(final Long jobId, final String action, final Throwable e) {
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
                job.status = JobStatus.FAILED;
                return job.persist();
              });
        });
  }
}
