package com.badu.entities.jobs;

import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.badu.entities.DataStatus;
import com.badu.entities.UpdatableEntity;
import com.badu.utils.EntityConstants;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Class represents a Job.
 *
 * It can be either scheduled or maintanance job or one time job, for example,
 * delete a document or semd email.
 */
@Getter
@Setter
@Entity
@Table(name = "JOBS")
public class Job extends UpdatableEntity {

  @Id
  @Column(name = "JOB_ID")
  @UuidGenerator
  private UUID id;

  @Column(name = "JOB_NAME", length = EntityConstants.COLUMN_NAME, nullable = false)
  private String name;

  @Column(name = "JOB_TYPE", length = EntityConstants.COLUMN_NAME, nullable = false)
  private String type;

  @Column(name = "JOB_DESCRIPTION", length = EntityConstants.COLUMN_LONG_DESC)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "JOB_EXECUTION_TYPE", nullable = false)
  private JobExecutionType executionType;

  @Column(name = "JOB_PARAMS", length = EntityConstants.COLUMN_LONG_DESC, nullable = false)
  private String params;

  @Column(name = "JOB_CRON_EXPR", length = EntityConstants.COLUMN_NAME)
  private String schedule;

  @Column(name = "MAX_RETRY_COUNTER", nullable = false)
  private int maxRetryCounter;

  @Column(name = "RETRY_DELAY")
  private int retryDelaySeconds;

  @Enumerated(EnumType.STRING)
  @Column(name = "JOB_STATE", nullable = false)
  private JobState state;

  @Enumerated(EnumType.STRING)
  @Column(name = "JOB_STATUS", nullable = false)
  private DataStatus status;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "job", cascade = CascadeType.ALL)
  private List<JobExecution> executions;

}
