package org.acme.hibernate.orm.panache;

import java.util.function.Supplier;

import org.jboss.logging.Logger;

import com.badu.repositories.JobExecutionRepository;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class FruitProcessingJob {

  private static final Logger LOG = Logger.getLogger(FruitProcessingJob.class);

  @Inject
  private JobExecutionRepository jobRepository;

  @Inject
  private FruitRepository fruitRepository;

  public Uni<Void> execute(final Fruit fruit, final Long jobId) {
    return Uni.createFrom().voidItem()
        .onItem().transformToUni(x -> {

          return executeAction(jobId, "Test Action 1", 30, () -> testAction(fruit.name + "_1"))
              .onItem().transformToUni(v1 -> executeAction(jobId, "Test Action 2", 60,
                  () -> testAction(fruit.name + "_2")))
              .onItem().transformToUni(v1 -> executeAction(jobId, "Test Action 3", 100,
                  () -> testAction(fruit.name + "_3")));

        });
  }

  private Uni<Void> executeAction(final Long jobId, final String actionName, final int progress,
      Supplier<Uni<Void>> action) {

    return Uni.createFrom().voidItem()
        .call(x -> {
          LOG.info("Execute action: " + actionName);
          return action.get()
              .onItem().call(v -> {
                return Panache.withTransaction(() -> jobRepository.recordJobSuccess(jobId, actionName, progress));
              })
              .onFailure().call(failure -> {
                return Panache.withTransaction(() -> jobRepository.recordJobFailure(jobId, actionName, failure));
              });
        });
  }

  private Uni<Void> testAction(final String fruitName) {
    return Panache.withTransaction(() -> fruitRepository.createFruit(fruitName)
        .onItem().ifNotNull().transformToUni(fruit -> Uni.createFrom().voidItem()));
  }

}