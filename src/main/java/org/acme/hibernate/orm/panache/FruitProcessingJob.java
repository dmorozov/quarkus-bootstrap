package org.acme.hibernate.orm.panache;

import java.util.function.Supplier;

import com.badu.repositories.JobRepository;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class FruitProcessingJob {

  @Inject
  private JobRepository jobRepository;

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
          return action.get()
              .onItem().call(v -> {
                return jobRepository.recordJobSuccess(jobId, actionName, progress);
              })
              .onFailure().call(failure -> {
                return jobRepository.recordJobFailure(jobId, actionName, failure);
              });
        });
  }

  private Uni<Void> testAction(final String fruitName) {
    return fruitRepository.createFruit(fruitName)
        .onItem().ifNotNull().transformToUni(fruit -> Uni.createFrom().voidItem());
  }

}