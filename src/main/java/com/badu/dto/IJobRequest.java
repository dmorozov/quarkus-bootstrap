package com.badu.dto;

import java.io.Serializable;
import java.util.UUID;

public interface IJobRequest extends Serializable {
  IJobRequest withJobId(final UUID jobId);
}
