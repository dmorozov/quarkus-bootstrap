package com.badu.utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.vertx.core.Context;
import io.vertx.core.Vertx;

public final class TransactionUtils {

  private static final String TX_ON_COMPLETE_CALLBACKS_KEY = "tx.on.complete.callbacks";

  private TransactionUtils() {
    // Utility class
  }

  public static void executePostTransactionCallbacks() {
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

  public static void registerPostTransactionCallback(Runnable callback) {
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
