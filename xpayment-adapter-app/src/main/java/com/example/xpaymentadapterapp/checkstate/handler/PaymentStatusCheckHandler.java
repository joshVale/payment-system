package com.example.xpaymentadapterapp.checkstate.handler;

import java.util.UUID;

public interface PaymentStatusCheckHandler {
    /**
     * Проверяет статус платежа в X Payment Provider по заданному
     * идентификатору. Если статус нетерминальный, то метод возвращает
     * false. В противном случае, отправляет асинхронное уведомление
     * Payment Service об измененном статусе платежа и возвращает true.
     *
     * @param chargeGuid UUID платежа в системе X Payment Provider для проверки
     * @return true, если платеж завершен и новые проверки статуса
     * не требуются, иначе false
     */
    boolean handle(UUID chargeGuid);
}
