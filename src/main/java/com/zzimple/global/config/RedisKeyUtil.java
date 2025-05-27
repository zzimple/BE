package com.zzimple.global.config;

import java.util.UUID;

public class RedisKeyUtil {

  public static String draftAddressKey(UUID draftId) {
    return String.format("estimate:draft:%s:address", draftId);
  }

  public static String draftMoveItemsKey(UUID draftId) {
    return String.format("estimate:draft:%s:move-items", draftId);
  }

  public static String previewHolidayKey(String date) {
    return String.format("estimate:preview:holiday:%s", date);
  }

  public static String draftMoveDateKey(UUID draftId) {
    return String.format("estimate:draft:%s:move-date", draftId);
  }

  public static String draftMoveTimeKey(UUID draftId) {
    return String.format("estimate:draft:%s:move-time", draftId);
  }

  public static String draftMoveTypeKey(UUID draftId) {
    return "estimate:draft:" + draftId + ":moveType";
  }

  public static String draftMoveOptionKey(UUID draftId) {
    return "draft:move-option:" + draftId;
  }
  public static String draftMoveHolidayKey(UUID draftId) {
    return String.format("estimate:draft:%s:move-holiday", draftId);
  }
}
