package ru.csu.iit.master2026.oot.hw01bank;

import java.util.Objects;

/** Счёт — сущность с идентичностью по AccountId и изменяемым балансом. */
public final class Account {
    private final AccountId id;
    private final long minimumBalance;
    private long balance;
    private Account(AccountId id, long minimumBalance) {
        this.id = Objects.requireNonNull(id);
        this.minimumBalance = minimumBalance;
    }
    public static Account debit(AccountId id) { return new Account(id, 0); }
    public static Account credit(AccountId id, Amount limit) {
        Objects.requireNonNull(limit);
        return new Account(id, Math.negateExact(limit.kopecks()));
    }
    public AccountId id() { return id; }
    public long balance() { return balance; }
    public Amount available() { return new Amount(Math.subtractExact(balance, minimumBalance)); }
    public void deposit(Amount amount) {
        Objects.requireNonNull(amount);
        balance = Math.addExact(balance, amount.kopecks());
    }
    public void withdraw(Amount amount) {
        Objects.requireNonNull(amount);
        if (!available().isAtLeast(amount)) throw new IllegalStateException("Недостаточно доступных средств");
        balance = Math.subtractExact(balance, amount.kopecks());
    }
    @Override public boolean equals(Object o) { return o instanceof Account a && id.equals(a.id); }
    @Override public int hashCode() { return id.hashCode(); }
}