package com.badu.dto;

import org.acme.hibernate.orm.panache.Fruit;

public class FruitJobRequest implements IJobRequest {
  public final Long jobId;
  public final Fruit fruit;

  public FruitJobRequest(final Long jobId, final Fruit fruit) {
    this.jobId = jobId;
    this.fruit = fruit;
  }
}
