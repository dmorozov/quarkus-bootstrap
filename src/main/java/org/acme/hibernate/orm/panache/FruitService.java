package org.acme.hibernate.orm.panache;

import java.util.List;

import com.badu.dto.FruitJobRequest;
import com.badu.services.jobs.JobService;
import com.badu.utils.TransactionUtils;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class FruitService {

  @Inject
  private FruitRepository fruitRepository;

  @Inject
  private JobService jobService;

  public Uni<Fruit> createFruit(final Fruit fruit) {
    return Panache.withTransaction(() -> {
      return fruitRepository.persist(fruit)
          .call(newFruit -> submitFruitProcessingJob(newFruit));
    }).onItem().invoke(TransactionUtils::executePostTransactionCallbacks);
  }

  private Uni<Void> submitFruitProcessingJob(final Fruit fruit) {
    // jobService.createJob(fruit)
    FruitJobRequest.builder()
        .fruit(fruit)
        .build();
    return Uni.createFrom().voidItem();
  }

  public Uni<Fruit> findById(Long id) {
    return Panache.withSession(() -> {
      return fruitRepository.findById(id);
    });
  }

  public Uni<Fruit> updateFruit(Long id, Fruit fruit) {
    return Panache.withTransaction(() -> fruitRepository.findById(id)
        .onItem().ifNotNull().invoke(entity -> entity.name = fruit.name));
  }

  public Uni<Boolean> deleteById(final Long id) {
    return Panache.withTransaction(() -> fruitRepository.deleteById(id));
  }

  public Uni<List<Fruit>> listAll() {
    return Panache.withSession(() -> {
      return fruitRepository.listAll(Sort.by("name"));
    });
  }
}
