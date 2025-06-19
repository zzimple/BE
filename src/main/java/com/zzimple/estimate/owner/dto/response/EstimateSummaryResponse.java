package com.zzimple.estimate.owner.dto.response;

public record EstimateSummaryResponse(
    int confirmedCount,
    int completedCount,
    int inProgressCount
) {}
