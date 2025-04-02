package com.badu.dto;

import java.util.UUID;

import org.acme.hibernate.orm.panache.Fruit;

import lombok.Builder;

@Builder(toBuilder = true)
public class FruitJobRequest implements IJobRequest {
  public final UUID jobId;
  public final Fruit fruit;
  public final UUID currentUserId;

  @Override
  public IJobRequest withJobId(final UUID jobId) {
    return this.toBuilder().jobId(jobId).build();
  }
}
