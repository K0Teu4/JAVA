package ru.csu.iit.master2026.oot.hw01bank;

import java.util.Objects;

/** Неизменяемая денежная сумма в копейках. */
public final class Amount {
    public static final Amount ZERO = new Amount(0);
    private final long kopecks;
    public Amount(long kopecks) {
        if (kopecks < 0) throw new IllegalArgumentException("Сумма не может быть отрицательной");
        this.kopecks = kopecks;
    }
    public static Amount rubles(long rubles) { return new Amount(Math.multiplyExact(rubles, 100)); }
    public long kopecks() { return kopecks; }
    public Amount plus(Amount other) { return new Amount(Math.addExact(kopecks, Objects.requireNonNull(other).kopecks)); }
    public Amount minus(Amount other) { return new Amount(Math.subtractExact(kopecks, Objects.requireNonNull(other).kopecks)); }
    public boolean isAtLeast(Amount other) { return kopecks >= Objects.requireNonNull(other).kopecks; }
    @Override public boolean equals(Object o) { return o instanceof Amount a && kopecks == a.kopecks; }
    @Override public int hashCode() { return Long.hashCode(kopecks); }
    @Override public String toString() { return "%d.%02d руб.".formatted(kopecks / 100, kopecks % 100); }
}