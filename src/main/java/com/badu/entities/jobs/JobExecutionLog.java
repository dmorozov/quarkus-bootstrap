package com.badu.entities.jobs;

import com.badu.utils.EntityConstants;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "JOB_EXECUTION_LOGS")
public class JobExecutionLog {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "JOB_EXECUTION_LOGS_SEQ")
  @SequenceGenerator(name = "JOB_EXECUTION_LOGS_SEQ", sequenceName = "JOB_EXECUTION_LOGS_SEQ")
  public Long id;

  @Column(name = "JOB_EXECUTION_ID", nullable = false)
  public Long jobExecutionId;

  @Column(name = "ACTION_NAME", length = EntityConstants.COLUMN_LONG_NAME, nullable = false)
  public String action;

  @Column(name = "PROGRESS", nullable = false)
  public int progress;

  @Column(name = "PROCESSING_DETAILS", length = EntityConstants.COLUMN_LONG_DESC)
  public String processingDetails;

  @Column(name = "ERROR_DETAILS", length = EntityConstants.COLUMN_LONG_DESC)
  public String errorDetails;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "JOB_EXECUTION_ID", insertable = false, updatable = false)
  public JobExecution jobExecution;
}
