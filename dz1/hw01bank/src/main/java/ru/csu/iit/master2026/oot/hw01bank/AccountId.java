package ru.csu.iit.master2026.oot.hw01bank;
import java.util.Objects;
/** Номер банковского счёта. */
public record AccountId(String value) {
 public AccountId { Objects.requireNonNull(value); if(value.isBlank()) throw new IllegalArgumentException("Пустой номер счёта"); }
 @Override public String toString(){return value;}
}