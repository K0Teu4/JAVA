package ru.csu.iit.master2026.oot.hw01bank;
import java.util.ArrayDeque; import java.util.Deque; import java.util.Objects;
/** Журнал хранит только успешно исполненные и ещё не отменённые команды. */
public final class Ledger {
    private final Deque<Command> history = new ArrayDeque<>();
    public void run(Command command) { Objects.requireNonNull(command); command.execute(); history.push(command); }
    public void undoLast() {
        if(history.isEmpty()) throw new IllegalStateException("Нет операций для отмены");
        Command latest=history.peek(); latest.undo(); history.pop();
    }
    public int size() { return history.size(); }
}