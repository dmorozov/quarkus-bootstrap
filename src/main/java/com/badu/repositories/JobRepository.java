package com.badu.repositories;

import com.badu.entities.jobs.JobInstance;
import com.badu.entities.jobs.JobLog;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JobRepository implements PanacheRepository<JobInstance> {

  public Uni<JobLog> recordJobSuccess(final Long jobId, final String action, final int progress) {

  }

  public Uni<JobLog> recordJobFailure(final Long jobId, final String action, final Throwable e) {

  }
}
