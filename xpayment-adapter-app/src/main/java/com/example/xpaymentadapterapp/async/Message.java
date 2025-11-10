package com.example.xpaymentadapterapp.async;

import java.time.Instant;
import java.util.UUID;

public interface Message {

    UUID messageId();

    Instant occurredAt();
}
