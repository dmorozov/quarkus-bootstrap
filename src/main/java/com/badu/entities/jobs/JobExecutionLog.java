package com.badu.entities.jobs;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.badu.entities.CreatableEntity;
import com.badu.utils.EntityConstants;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "JOB_EXECUTION_LOGS")
public class JobExecutionLog extends CreatableEntity {

  @Id
  @Column(name = "JOB_EXECUTION_LOG_ID")
  @UuidGenerator
  private UUID id;

  @Column(name = "JOB_EXECUTION_ID", nullable = false)
  private UUID jobExecutionId;

  @Column(name = "ACTION_NAME", length = EntityConstants.COLUMN_LONG_NAME, nullable = false)
  private String action;

  @Column(name = "PROGRESS", nullable = false)
  private int progress;

  @Column(name = "PROCESSING_DETAILS", length = EntityConstants.COLUMN_LONG_DESC)
  private String processingDetails;

  @Column(name = "ERROR_DETAILS", length = EntityConstants.COLUMN_LONG_DESC)
  private String errorDetails;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "JOB_EXECUTION_ID", insertable = false, updatable = false)
  private JobExecution jobExecution;
}
