package com.badu.entities;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@MappedSuperclass
public class UpdatableEntity {

  @Column(name = "CREATED_ON", nullable = false)
  private ZonedDateTime createdTime;

  @Column(name = "CREATED_BY", nullable = false)
  private UUID createdBy;

  @Version
  @Column(name = "UPDATED_ON", nullable = false)
  private ZonedDateTime modifiedTime;

  @Column(name = "UPDATED_BY", nullable = false)
  private UUID modifiedBy;
}
