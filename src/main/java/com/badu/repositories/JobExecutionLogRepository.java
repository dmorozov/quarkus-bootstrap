package com.badu.repositories;

import com.badu.entities.jobs.JobExecutionLog;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JobExecutionLogRepository implements PanacheRepository<JobExecutionLog> {
}
