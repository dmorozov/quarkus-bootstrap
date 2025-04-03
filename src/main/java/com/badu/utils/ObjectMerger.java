package com.badu.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ObjectMerger {

  private ObjectMerger() {
    // Utility Class
  }

  public static <T> T merge(T target, T source) {
    if (target == null || source == null) {
      return target == null ? source : target;
    }

    Class<?> clazz = target.getClass();
    Field[] fields = clazz.getDeclaredFields();

    for (Field field : fields) {
      String fieldName = field.getName();
      String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
      String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

      try {
        Method getter = clazz.getMethod(getterName);
        Method setter = clazz.getMethod(setterName, field.getType());

        Object sourceValue = getter.invoke(source);
        Object targetValue = getter.invoke(target);

        if (sourceValue != null) {
          if (targetValue != null && field.getType().isInstance(targetValue)) {
            if (!ObjectMerger.isPrimitiveOrString(field.getType())) {
              sourceValue = merge(targetValue, sourceValue);
            }
          }
          setter.invoke(target, sourceValue);
        }
      } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        // Handle exceptions as needed (e.g., log or throw)
        System.err.println("Error merging field " + fieldName + ": " + e.getMessage());
      }
    }
    return target;
  }

  private static boolean isPrimitiveOrString(Class<?> type) {
    return type.isPrimitive() ||
        type == String.class ||
        type == Integer.class ||
        type == Long.class ||
        type == Boolean.class ||
        type == Float.class ||
        type == Double.class ||
        type == Character.class ||
        type == Short.class;
  }
}
