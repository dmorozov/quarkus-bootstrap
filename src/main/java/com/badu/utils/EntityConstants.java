package com.badu.utils;

public final class EntityConstants {

  private EntityConstants() {
    // Utility classes should not have public constructor
  }

  // Entity columns length
  public static final int COLUMN_EMAIL = 255;
  public static final int COLUMN_PASSWORD = 100;
  public static final int COLUMN_NAME = 50;
  public static final int COLUMN_LONG_NAME = 255;
  public static final int COLUMN_LONG_DESC = 4096;
  public static final int COLUMN_UUID = 36;
  public static final int COLUMN_PHONE = 20;

  public static final int VECTOR_TAG_COUNT = 10;
}
