package ru.csu.iit.master2026.oot.hw01orderflow.domain;

/** Стратегия расчёта стоимости и срока доставки. */
public interface DeliveryTariff {
    Money cost(Money goods);
    int days();

    /** Явное свойство тарифа, не зависящее от конкретного класса. */
    default boolean express() { return false; }

    /** Текстовое обещание, добавляемое к уведомлению о создании заказа. */
    default String placementMessage() { return ""; }
}
