package com.example.paymentserviceapp.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.ZoneOffset;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeConstants {

    public static final ZoneOffset DEFAULT_ZONE_OFFSET = ZoneOffset.UTC;
}