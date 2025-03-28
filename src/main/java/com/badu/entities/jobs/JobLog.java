package com.badu.entities.jobs;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "JOB_LOGS")
public class JobLog extends PanacheEntity {

  @Column(name = "JOB_ID", nullable = false)
  public Long jobId;

  @Column(name = "ACTION_NAME", length = 256, nullable = false)
  public String action;

  @Column(name = "PROGRESS", nullable = false)
  public int progress;

  @Column(name = "PROCESSING_DETAILS", length = 2048)
  public String processingDetails;

  @Column(name = "ERROR_DETAILS", length = 2048)
  public String errorDetails;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "JOB_ID", nullable = false, insertable = false, updatable = false)
  public JobInstance job;
}
