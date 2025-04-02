package com.badu.entities.jobs;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.badu.entities.CreatableEntity;
import com.badu.utils.EntityConstants;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "JOB_EXECUTIONS")
public class JobExecution extends CreatableEntity {

  @Id
  @Column(name = "JOB_EXECUTION_ID")
  @UuidGenerator
  private UUID id;

  @Column(name = "JOB_ID", nullable = false)
  private UUID jobId;

  @Column(name = "JOB_NAME", length = EntityConstants.COLUMN_NAME, nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "JOB_STATE", nullable = false)
  private JobState state;

  @Column(name = "TRIGGERED_BY", nullable = false)
  private UUID triggeredBy;

  @Column(name = "RETRY_COUNTER", nullable = false)
  private int retryCounter;

  @Column(name = "PROGRESS", nullable = false)
  private int progress;

  @Column(name = "START_TIME", nullable = false)
  private ZonedDateTime startTime;

  @Column(name = "END_TIME")
  private ZonedDateTime endTime;

  @Column(name = "ERROR_DETAILS", length = EntityConstants.COLUMN_LONG_DESC)
  private String errorDetails;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "JOB_ID", insertable = false, updatable = false)
  private Job job;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "job", cascade = CascadeType.ALL)
  private List<JobExecutionLog> logs;

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "<" + id + ">";
  }
}
