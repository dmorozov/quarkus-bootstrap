package com.badu.entities.jobs;

import java.util.List;

import com.badu.utils.EntityConstants;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "JOBS")
public class Job {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "JOBS_SEQ")
  @SequenceGenerator(name = "JOBS_SEQ", sequenceName = "JOBS_SEQ")
  public Long id;

  @Column(name = "JOB_NAME", length = EntityConstants.COLUMN_NAME, nullable = false)
  private String name;

  @Column(name = "JOB_PAYLOAD", length = EntityConstants.COLUMN_LONG_DESC, nullable = false)
  private String payload;

  @Column(name = "RETRY_COUNTER", nullable = false)
  private int retryCounter;

  @Column(name = "MAX_RETRY_COUNTER", nullable = false)
  private int maxRetryCounter;

  @Enumerated(EnumType.STRING)
  @Column(name = "JOB_STATUS", nullable = false)
  private JobStatus status;

  @Column(name = "PROGRESS", nullable = false)
  private int progress;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "job", cascade = CascadeType.ALL)
  private List<JobExecution> executions;

  public JobExecution toExecution() {
    JobExecution jobExecution = new JobExecution();
    jobExecution.jobId = id;
    jobExecution.name = name;
    jobExecution.status = JobStatus.PENDING;

    return jobExecution;
  }
}
