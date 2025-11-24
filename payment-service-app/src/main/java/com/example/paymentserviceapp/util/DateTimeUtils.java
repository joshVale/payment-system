package com.example.paymentserviceapp.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.OffsetDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateTimeUtils {

    public static OffsetDateTime toOffsetDateTimeOrMin(Instant instant) {
        return instant != null
                ? OffsetDateTime.ofInstant(instant, TimeConstants.DEFAULT_ZONE_OFFSET)
                : OffsetDateTime.MIN;
    }

    public static OffsetDateTime toOffsetDateTimeOrMax(Instant instant) {
        return instant != null
                ? OffsetDateTime.ofInstant(instant, TimeConstants.DEFAULT_ZONE_OFFSET)
                : OffsetDateTime.MAX;
    }
}