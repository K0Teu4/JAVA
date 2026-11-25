package ru.csu.iit.master2026.oot.hw01orderflow.app;

import org.junit.jupiter.api.Test;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.*;
import ru.csu.iit.master2026.oot.hw01orderflow.infra.ConsoleNotifier;
import ru.csu.iit.master2026.oot.hw01orderflow.infra.InMemoryOrderRepository;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.csu.iit.master2026.oot.hw01orderflow.domain.Money.rubles;

class HomeworkRequirementsTest {
    private Customer customer() {
        return new Customer(new CustomerId("c-1"), "Анна", new Email("anna@example.ru"), "+79000000001");
    }
    private Notifier silent() {
        return new Notifier() {
            public void orderPlaced(Order o) {}
            public void orderPaid(Order o) {}
            public void orderShipped(Order o) {}
            public void orderCancelled(Order o, Money m) {}
        };
    }

    @Test void sameDayDelivery_hasRequiredPriceDaysAndNotice() {
        SameDayDelivery tariff = new SameDayDelivery();
        assertThat(tariff.cost(rubles(350))).isEqualTo(rubles(900));
        assertThat(tariff.cost(rubles(100_000))).isEqualTo(rubles(900));
        assertThat(tariff.days()).isZero();
        assertThat(tariff.express()).isTrue();

        var repo = new InMemoryOrderRepository();
        var order = new PlaceOrder(repo, new ConsoleNotifier()).place(
            Order.builder().customer(customer()).item("Кружка", 1, rubles(350)).delivery(tariff));
        assertThat(order.total()).isEqualTo(rubles(1250));
    }

    @Test void sameDayNotice_isPrintedByNotifier() {
        var repo = new InMemoryOrderRepository();
        var bytes = new ByteArrayOutputStream();
        PrintStream previous = System.out;
        try {
            System.setOut(new PrintStream(bytes, true, StandardCharsets.UTF_8));
            new PlaceOrder(repo, new ConsoleNotifier()).place(
                Order.builder().customer(customer()).item("Кружка", 1, rubles(350)).delivery(new SameDayDelivery()));
        } finally { System.setOut(previous); }
        assertThat(bytes.toString(StandardCharsets.UTF_8))
            .contains("Экспресс-доставка: привезём сегодня.");
    }

    @Test void paidOnlyReport_filtersRowsAndEveryTotal() {
        var repo = new InMemoryOrderRepository();
        var place = new PlaceOrder(repo, silent());
        Order first = place.place(Order.builder().customer(customer()).item("Ноутбук", 1, rubles(64990)).delivery(new ExpressDelivery()));
        first.pay();
        place.place(Order.builder().customer(customer()).item("Кружка", 1, rubles(350)).delivery(new CourierDelivery()));
        Order third = place.place(Order.builder().customer(customer()).item("Кружка", 2, rubles(350)).delivery(new Pickup()));
        third.pay(); third.ship();

        assertThat(new PaidOnlySalesReport(repo).render()).isEqualTo(
            "ORD-1  PAID      65490.00 руб.\\n" +
            "ORD-3  SHIPPED   700.00 руб.\\n" +
            "Оплаченных заказов: 2, экспресс: 1, выручка: 66190.00 руб., крупнейший: ORD-1");
    }

    @Test void paidOnlyReport_emptyRepositoryHasExactFooter() {
        assertThat(new PaidOnlySalesReport(new InMemoryOrderRepository()).render())
            .isEqualTo("Оплаченных заказов: 0, экспресс: 0, выручка: 0.00 руб., крупнейший: —");
    }

    @Test void delivered_isOnlyAllowedAfterShippingAndIsTerminalPaidStatus() {
        var order = new PlaceOrder(new InMemoryOrderRepository(), silent()).place(
            Order.builder().customer(customer()).item("x", 1, rubles(10)).delivery(new Pickup()));
        order.pay(); order.ship(); order.deliver();
        assertThat(order.status()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(order.status().paid()).isTrue();
        assertThatThrownBy(order::deliver).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(order::cancel).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(order::ship).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(order::pay).isInstanceOf(IllegalStateException.class);
        assertThat(order.status()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test void cannotDeliverNewOrderAndFailedTransitionDoesNotChangeStatus() {
        var order = new PlaceOrder(new InMemoryOrderRepository(), silent()).place(
            Order.builder().customer(customer()).item("x", 1, rubles(10)).delivery(new Pickup()));
        assertThatThrownBy(order::deliver).isInstanceOf(IllegalStateException.class);
        assertThat(order.status()).isEqualTo(OrderStatus.NEW);
    }
}