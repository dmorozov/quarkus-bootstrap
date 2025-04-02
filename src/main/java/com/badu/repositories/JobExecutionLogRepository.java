package com.badu.repositories;

import java.util.UUID;

import com.badu.entities.jobs.JobExecutionLog;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JobExecutionLogRepository implements PanacheRepositoryBase<JobExecutionLog, UUID> {
}
