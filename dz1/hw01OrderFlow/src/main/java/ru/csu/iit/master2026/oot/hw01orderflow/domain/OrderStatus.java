package ru.csu.iit.master2026.oot.hw01orderflow.domain;

/** Паттерн «Состояние»: статус определяет разрешённые переходы заказа. */
public enum OrderStatus {
    NEW {
        @Override OrderStatus pay() { return PAID; }
        @Override OrderStatus cancel() { return CANCELLED; }
    },
    PAID {
        @Override OrderStatus ship() { return SHIPPED; }
        @Override OrderStatus cancel() { return CANCELLED; }
        @Override public boolean paid() { return true; }
    },
    SHIPPED {
        @Override OrderStatus deliver() { return DELIVERED; }
        @Override public boolean paid() { return true; }
    },
    DELIVERED {
        @Override public boolean paid() { return true; }
    },
    CANCELLED;

    OrderStatus pay() { throw forbidden("оплатить"); }
    OrderStatus ship() { throw forbidden("отправить"); }
    OrderStatus cancel() { throw forbidden("отменить"); }
    OrderStatus deliver() { throw forbidden("вручить"); }
    public boolean paid() { return false; }
    private IllegalStateException forbidden(String action) {
        return new IllegalStateException("Нельзя " + action + " заказ в статусе " + this);
    }
}