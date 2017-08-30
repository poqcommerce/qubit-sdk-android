package com.qubit.android.sdk.internal.util;

/**
 * Created by aamq on 30.08.2017.
 */

public final class DateTimeUtils {

  private static final int SEC_IN_MIN = 60;
  private static final int MS_IN_SEC = 1000;

  private DateTimeUtils() {
  }

  public static long minToMs(long min) {
    return min * SEC_IN_MIN * MS_IN_SEC;
  }
}