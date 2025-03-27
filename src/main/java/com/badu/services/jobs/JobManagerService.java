package com.badu.services.jobs;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import io.smallrye.mutiny.Uni;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class JobManagerService {

  @Inject
  private Vertx vertx;

  private final ExecutorService executor = Executors.newFixedThreadPool(10);

  private <T> Uni<T> executeWithContext(Supplier<Uni<T>> action) {
    return Uni.createFrom().deferred(() -> {
      // Create a new context explicitly
      Context newContext = vertx.getOrCreateContext();

      // Create a completable future that will resolve with the result
      CompletableFuture<T> future = new CompletableFuture<>();

      // Run on the executor service
      executor.execute(() -> {
        // Run on the new context
        newContext.runOnContext(v -> {
          // Execute the action and handle its result
          action.get()
              .subscribe()
              .with(
                  result -> future.complete(result),
                  failure -> future.completeExceptionally(failure));
        });
      });

      // Convert the completable future to a Uni
      return Uni.createFrom().completionStage(future);
    });
  }

  public <T> Uni<Void> submitJob(Supplier<Uni<T>> action) {
    return executeWithContext(() -> {
      return action.get();
    }).onItem().transformToUni(r -> {
      return Uni.createFrom().voidItem();
    });
  }
}