package com.badu.utils;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.badu.entities.CreatableEntity;
import com.badu.entities.UpdatableEntity;

public final class EntityUtils {

  private EntityUtils() {
    // private constructor for utility class
  }

  public static void updateUserContext(final UUID userId,
      final UpdatableEntity entity) {
    if (entity.getCreatedBy() == null) {
      entity.setCreatedBy(userId);
      entity.setCreatedTime(ZonedDateTime.now());
    }
    entity.setModifiedBy(userId);
    entity.setModifiedTime(ZonedDateTime.now());
  }

  public static void updateUserContext(final UUID userId, final CreatableEntity entity) {
    if (entity.getCreatedBy() == null) {
      entity.setCreatedBy(userId);
      entity.setCreatedTime(ZonedDateTime.now());
    }
  }
}
