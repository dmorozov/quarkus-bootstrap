package com.badu.entities.jobs;

import java.util.List;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;

@Entity
public class JobInstance extends PanacheEntity {

  public enum JobStatus {
    PENDING, PROCESSING, SUCCEEDED, FAILED
  }

  @Column(name = "JOB_NAME", length = 40, unique = true)
  public String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "JOB_STATUS", nullable = false)
  public JobStatus status;

  // @OneToMany(fetch = FetchType.EAGER, mappedBy = "job", cascade =
  // CascadeType.ALL)
  // private List<JobLog> logs;
}
