package org.acme.hibernate.orm.panache;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.reactive.messaging.Channel;

import com.badu.dto.FruitJobRequest;
import com.badu.dto.IJobRequest;
import com.badu.services.jobs.JobService;
import com.badu.utils.TransactionUtils;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class FruitService {

  @Inject
  private FruitRepository fruitRepository;

  @Inject
  private JobService jobService;

  @Channel(FruitJobRequest.JOB_TYPE)
  private MutinyEmitter<FruitJobRequest> msgEmitter;

  public Uni<Fruit> createFruit(final Fruit fruit) {
    return Panache.withTransaction(() -> {
      return fruitRepository.persist(fruit)
          .call(newFruit -> submitFruitProcessingJob(newFruit));
    }).onItem().invoke(TransactionUtils::executePostTransactionCallbacks);
  }

  private Uni<Void> submitFruitProcessingJob(final Fruit fruit) {

    FruitJobRequest jobRequest = FruitJobRequest.builder()
        .fruit(fruit)
        .triggeredBy(UUID.randomUUID())
        .build();

    return jobService.scheduleOneTimeJob(jobRequest, (jobId) -> {
      return msgEmitter.send((FruitJobRequest) jobRequest.withJobId(jobId));
    });
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
