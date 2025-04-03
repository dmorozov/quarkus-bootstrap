package com.badu.repositories;

import java.util.UUID;
import com.badu.entities.jobs.Job;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JobRespository implements PanacheRepositoryBase<Job, UUID> {
}
