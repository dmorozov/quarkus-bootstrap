package com.badu.entities;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class CreatableEntity {

  @Column(name = "CREATED_ON", nullable = false)
  private ZonedDateTime createdTime;

  @Column(name = "CREATED_BY", nullable = false)
  private UUID createdBy;
}
