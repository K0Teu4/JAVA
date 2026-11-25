package ru.csu.iit.master2026.oot.hw01bank;
/** Команда с поддержкой отмены. */
public interface Command { void execute(); void undo(); }