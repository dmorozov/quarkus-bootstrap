package com.badu.services.jobs;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.acme.hibernate.orm.panache.Fruit;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.logging.Logger;

import com.badu.dto.FruitJobRequest;
import com.badu.entities.jobs.JobExecution;
import com.badu.entities.jobs.JobExecution.JobStatus;
import com.badu.repositories.JobExecutionRepository;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class JobService {

  @Channel("TEST_MSG")
  private MutinyEmitter<FruitJobRequest> msgEmitter;

  @Inject
  private JobExecutionRepository jobRepository;

  private static final Logger LOG = Logger.getLogger(JobService.class);
  private static final String TX_ON_COMPLETE_CALLBACKS_KEY = "tx.on.complete.callbacks";

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

      return jobRepository.persist(job)
          .chain(jobExecution -> {
            return postTransactionCommit(jobExecution, fruit);
          });
    }).onItem().invoke(this::executePostTransactionCallbacks);
  }

  private void executePostTransactionCallbacks() {
    Context context = Vertx.currentContext();
    if (context == null) {
      return;
    }

    List<Runnable> callbacks = context.getLocal(TX_ON_COMPLETE_CALLBACKS_KEY);

    if (callbacks != null) {
      // Execute all callbacks
      for (Runnable callback : callbacks) {
        callback.run();
      }
      // Clean up
      context.removeLocal(TX_ON_COMPLETE_CALLBACKS_KEY);
    }
  }

  private Uni<JobExecution> postTransactionCommit(final JobExecution jobExecution, final Fruit fruit) {

    registerPostTransactionCallback(() -> {
      msgEmitter.send(new FruitJobRequest(jobExecution.id, fruit))
          .subscribe()
          .with(
              result -> LOG.debug("Fruit job processing event sent."),
              failure -> LOG.error("Unable to trigger Fruit job processing event.", failure));
    });

    return Uni.createFrom().item(jobExecution);
  }

  private void registerPostTransactionCallback(Runnable callback) {
    // Get current Vertx context
    Context context = Vertx.currentContext();
    if (context == null) {
      throw new IllegalStateException("No Vertx context available");
    }

    // Get or create callback list for current transaction
    List<Runnable> callbacks = context.getLocal(TX_ON_COMPLETE_CALLBACKS_KEY);

    if (callbacks == null) {
      callbacks = new CopyOnWriteArrayList<>();
      context.putLocal(TX_ON_COMPLETE_CALLBACKS_KEY, callbacks);
    }

    // Add callback to list
    callbacks.add(callback);
  }
}
