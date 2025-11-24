package com.example.paymentserviceapp.async;

import java.time.Instant;
import java.util.UUID;

public interface Message {

    UUID messageId();

    Instant occurredAt();
}
