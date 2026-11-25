package ru.csu.iit.master2026.oot.hw01bank;
import java.util.Objects;
/** Атомарный перевод. При сбое второй части первая компенсируется. */
public final class Transfer implements Command {
    private final Account from, to; private final Amount amount; private boolean executed;
    public Transfer(Account from, Account to, Amount amount) {
        this.from=Objects.requireNonNull(from); this.to=Objects.requireNonNull(to); this.amount=Objects.requireNonNull(amount);
        if(this.from.equals(this.to)) throw new IllegalArgumentException("Нельзя переводить на тот же счёт");
    }
    @Override public void execute() {
        if(executed) throw new IllegalStateException("Команда уже выполнена");
        from.withdraw(amount);
        try { to.deposit(amount); }
        catch(RuntimeException failure) { from.deposit(amount); throw failure; }
        executed=true;
    }
    @Override public void undo() {
        if(!executed) throw new IllegalStateException("Команда не выполнена");
        to.withdraw(amount);
        try { from.deposit(amount); }
        catch(RuntimeException failure) { to.deposit(amount); throw failure; }
        executed=false;
    }
}