package ru.csu.iit.master2026.oot.hw01orderflow.app;

import ru.csu.iit.master2026.oot.hw01orderflow.domain.Order;
import java.util.List;

/** Отчёт только по оплаченным заказам; все показатели вычисляются по той же выборке. */
public final class PaidOnlySalesReport extends SalesReport {
    public PaidOnlySalesReport(OrderRepository orders) { super(orders); }
    @Override protected List<Order> selectedOrders() {
        return orders.findAll().stream().filter(order -> order.status().paid()).toList();
    }
    @Override protected void row(StringBuilder out, Order order) {
        out.append("%-6s %-9s %s\\n".formatted(order.id(), order.status(), order.total()));
    }
    @Override protected void footer(StringBuilder out, Totals totals) {
        out.append("Оплаченных заказов: ").append(totals.count())
            .append(", экспресс: ").append(totals.express())
            .append(", выручка: ").append(totals.revenue())
            .append(", крупнейший: ").append(totals.largest().map(Object::toString).orElse("—"));
    }
}