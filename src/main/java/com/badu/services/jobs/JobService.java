package com.badu.services.jobs;

import java.util.function.Supplier;

import org.acme.hibernate.orm.panache.Fruit;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.logging.Logger;

import com.badu.dto.FruitJobRequest;
import com.badu.dto.IJobRequest;
import com.badu.entities.jobs.JobExecution;
import com.badu.entities.jobs.JobState;
import com.badu.repositories.JobExecutionRepository;
import com.badu.utils.TransactionUtils;

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

  private static final Logger LOG = Logger.getLogger(JobService.class);

  public final Uni<JobExecution> createJob(final IJobRequest jobRequest, Supplier<Uni<Void>> onCommitCallback) {
    // TODO: implement new job
    // 1. Create and persist JobInstance entity
    // 2. Send job request to channel
    // Note: we do not want to hardcode/define all channels in one service. Not
    // scalable and tight-coupled. How to offload to the job request itself?

    // FruitJobRequest

    return Panache.withTransaction(() -> {
      JobExecution job = new JobExecution();
      job.name = "Fruit Processing";
      job.status = JobState.PENDING;

      return jobRepository.persist(job)
          .chain(jobExecution -> {
            TransactionUtils.registerPostTransactionCallback(() -> {
              msgEmitter.send(new FruitJobRequest(jobExecution.id, fruit))
                  .subscribe()
                  .with(
                      result -> LOG.debug("Fruit job processing event sent."),
                      failure -> LOG.error("Unable to trigger Fruit job processing event.", failure));
            });

            return Uni.createFrom().item(jobExecution);
          });
    });
  }
}
