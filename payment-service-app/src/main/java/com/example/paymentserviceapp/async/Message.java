package com.example.paymentserviceapp.async;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface Message {

    UUID messageId();

    OffsetDateTime occurredAt();
}
