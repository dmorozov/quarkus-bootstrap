package com.badu.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class JobConfig {

  public static final int DEFAULT_DELAY = 3; // in seconds
  public static final int DEFAULT_MAX_RETRY = 3;

  @Builder.Default
  private int retryDelaySeconds = DEFAULT_DELAY;

  @Builder.Default
  private int maxRetryCounter = DEFAULT_MAX_RETRY;
}
