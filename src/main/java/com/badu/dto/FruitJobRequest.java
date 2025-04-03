package com.badu.dto;

import java.util.UUID;

import org.acme.hibernate.orm.panache.Fruit;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class FruitJobRequest implements IJobRequest {

  public static final String JOB_TYPE = "FRUIT_CREATED";

  public final UUID jobId;
  public final String jobName;
  public final UUID triggeredBy;
  public final String jobType = FruitJobRequest.JOB_TYPE;

  public final Fruit fruit;

  @Override
  public IJobRequest withJobId(final UUID jobId) {
    return this.toBuilder().jobId(jobId).build();
  }
}
