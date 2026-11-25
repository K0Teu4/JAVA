package ru.csu.iit.master2026.oot.hw01bank;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class BankOperationsTest {
    @Test void amount_isImmutableNonNegativeAndComparedByValue() {
        assertThat(new Amount(12345)).isEqualTo(new Amount(12345));
        assertThat(new Amount(12345).hashCode()).isEqualTo(new Amount(12345).hashCode());
        assertThat(new Amount(12345).toString()).isEqualTo("123.45 руб.");
        assertThatThrownBy(() -> new Amount(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Amount(1).minus(new Amount(2))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test void debitAndCreditAccountsRespectTheirLimits() {
        Account debit = Account.debit(new AccountId("D-1"));
        assertThatThrownBy(() -> debit.withdraw(Amount.rubles(1))).isInstanceOf(IllegalStateException.class);
        debit.deposit(Amount.rubles(25)); debit.withdraw(Amount.rubles(25));
        assertThat(debit.balance()).isZero();
        Account credit = Account.credit(new AccountId("C-1"), Amount.rubles(100));
        credit.withdraw(Amount.rubles(100));
        assertThat(credit.balance()).isEqualTo(-10_000);
        assertThat(credit.available()).isEqualTo(Amount.ZERO);
        assertThatThrownBy(() -> credit.withdraw(new Amount(1))).isInstanceOf(IllegalStateException.class);
    }

    @Test void accountIdentityIsAccountIdRatherThanBalance() {
        Account a = Account.debit(new AccountId("same"));
        Account b = Account.debit(new AccountId("same"));
        a.deposit(Amount.rubles(99));
        assertThat(a).isEqualTo(b);
    }

    @Test void ledgerRecordsOnlySuccessfulCommandsAndUndoesInLifoOrder() {
        Account account = Account.debit(new AccountId("L-1"));
        Ledger ledger = new Ledger();
        assertThatThrownBy(() -> ledger.run(new Withdraw(account, Amount.rubles(1))))
            .isInstanceOf(IllegalStateException.class);
        assertThat(ledger.size()).isZero();
        ledger.run(new Deposit(account, Amount.rubles(100)));
        ledger.run(new Withdraw(account, Amount.rubles(30)));
        assertThat(account.balance()).isEqualTo(7_000);
        assertThat(ledger.size()).isEqualTo(2);
        ledger.undoLast();
        assertThat(account.balance()).isEqualTo(10_000);
        ledger.undoLast();
        assertThat(account.balance()).isZero();
        assertThat(ledger.size()).isZero();
        assertThatThrownBy(ledger::undoLast).isInstanceOf(IllegalStateException.class);
    }

    @Test void failedDepositUndoLeavesAccountUnchangedAndCommandInLedger() {
        Account account = Account.debit(new AccountId("D-2"));
        Ledger ledger = new Ledger();
        ledger.run(new Deposit(account, Amount.rubles(20)));
        account.withdraw(Amount.rubles(20));
        assertThatThrownBy(ledger::undoLast).isInstanceOf(IllegalStateException.class);
        assertThat(account.balance()).isZero();
        assertThat(ledger.size()).isEqualTo(1);
    }

    @Test void transferAndUndoChangeBothAccounts() {
        Account from = Account.debit(new AccountId("T-1"));
        Account to = Account.debit(new AccountId("T-2"));
        from.deposit(Amount.rubles(500));
        Transfer transfer = new Transfer(from, to, Amount.rubles(200));
        transfer.execute();
        assertThat(from.balance()).isEqualTo(30_000);
        assertThat(to.balance()).isEqualTo(20_000);
        transfer.undo();
        assertThat(from.balance()).isEqualTo(50_000);
        assertThat(to.balance()).isZero();
    }

    @Test void transferWithInsufficientFundsChangesNeitherAccount() {
        Account from = Account.debit(new AccountId("T-3"));
        Account to = Account.debit(new AccountId("T-4"));
        assertThatThrownBy(() -> new Transfer(from, to, Amount.rubles(1)).execute())
            .isInstanceOf(IllegalStateException.class);
        assertThat(from.balance()).isZero();
        assertThat(to.balance()).isZero();
    }

    @Test void transferRollsBackDebitWhenCreditWouldOverflow() {
        Account from = Account.debit(new AccountId("T-5"));
        Account to = Account.debit(new AccountId("T-6"));
        from.deposit(Amount.rubles(10));
        to.deposit(new Amount(Long.MAX_VALUE));
        assertThatThrownBy(() -> new Transfer(from, to, new Amount(1)).execute())
            .isInstanceOf(ArithmeticException.class);
        assertThat(from.balance()).isEqualTo(1_000);
        assertThat(to.balance()).isEqualTo(Long.MAX_VALUE);
    }

    @Test void failedTransferUndoRestoresDestinationAndKeepsCommandUndoable() {
        Account from = Account.debit(new AccountId("T-7"));
        Account to = Account.debit(new AccountId("T-8"));
        from.deposit(Amount.rubles(10));
        Transfer transfer = new Transfer(from, to, Amount.rubles(5));
        transfer.execute();
        from.deposit(new Amount(Long.MAX_VALUE - 500));
        long max = from.balance();
        assertThatThrownBy(transfer::undo).isInstanceOf(ArithmeticException.class);
        assertThat(from.balance()).isEqualTo(max);
        assertThat(to.balance()).isEqualTo(500);
    }

    @Test void transferRejectsSameAccountIdentity() {
        Account first = Account.debit(new AccountId("same"));
        Account second = Account.debit(new AccountId("same"));
        assertThatThrownBy(() -> new Transfer(first, second, Amount.rubles(1)))
            .isInstanceOf(IllegalArgumentException.class);
    }
}