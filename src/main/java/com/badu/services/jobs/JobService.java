package com.badu.services.jobs;

import org.acme.hibernate.orm.panache.Fruit;
import org.eclipse.microprofile.reactive.messaging.Channel;

import com.badu.dto.FruitJobRequest;
import com.badu.entities.jobs.JobExecution;
import com.badu.entities.jobs.JobExecution.JobStatus;
import com.badu.repositories.JobExecutionRepository;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class JobService {

  @Channel("TEST_MSG")
  private MutinyEmitter<FruitJobRequest> msgEmitter;

  @Inject
  private JobExecutionRepository jobRepository;

  public final Uni<JobExecution> createJob(final Fruit fruit) {
    // TODO: implement new job
    // 1. Create and persist JobInstance entity
    // 2. Send job request to channel
    // Note: we do not want to hardcode/define all channels in one service. Not
    // scalable and tight-coupled. How to offload to the job request itself?

    return Panache.withTransaction(() -> {
      JobExecution job = new JobExecution();
      job.name = "Fruit Processing";
      job.status = JobStatus.PENDING;

      return jobRepository.persist(job);
    }).onItem().ifNotNull().call(job -> {
      return msgEmitter.send(new FruitJobRequest(job.id, fruit));
    });
  }

}
