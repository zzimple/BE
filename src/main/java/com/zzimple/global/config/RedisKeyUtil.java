package com.zzimple.global.config;

import java.util.UUID;

public class RedisKeyUtil {

  public static String draftAddressKey(UUID draftId) {
    return String.format("estimate:draft:%s:address", draftId);
  }

  public static String draftMoveItemsKey(UUID draftId) {
    return String.format("estimate:draft:%s:move-items", draftId);
  }

  public static String draftHolidayKey(UUID draftId, String date) {
    return String.format("estimate:draft:%s:holiday:%s", draftId, date);
  }
}
