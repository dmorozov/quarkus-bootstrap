package com.badu.dto;

import java.util.UUID;

import org.acme.hibernate.orm.panache.Fruit;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class FruitJobRequest implements IJobRequest {

  public static final String JOB_TYPE = "FRUIT_CREATED";

  private UUID jobId;
  private String jobName;
  private UUID triggeredBy;

  @Builder.Default
  private String jobType = FruitJobRequest.JOB_TYPE;

  private Fruit fruit;

  @Override
  public IJobRequest withJobId(final UUID jobId) {
    return this.toBuilder().jobId(jobId).build();
  }
}
