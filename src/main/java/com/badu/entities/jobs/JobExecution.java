package com.badu.entities.jobs;

import java.util.List;

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
@Table(name = "JOB_EXECUTIONS")
public class JobExecution {

  public enum JobStatus {
    PENDING, PROCESSING, SUCCEEDED, FAILED
  }

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "JOB_EXECUTIONS_SEQ")
  @SequenceGenerator(name = "JOB_EXECUTIONS_SEQ", sequenceName = "JOB_EXECUTIONS_SEQ")
  public Long id;

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "<" + id + ">";
  }

  @Column(name = "JOB_NAME", length = 40, unique = true)
  public String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "JOB_STATUS", nullable = false)
  public JobStatus status;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "job", cascade = CascadeType.ALL)
  private List<JobExecutionLog> logs;
}
