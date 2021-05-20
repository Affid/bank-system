package com.sberstart.affid.banksystem.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Card {
    private final String id;
    private final PaymentSystem paymentSystem;
    private final String bankId;
    private final int control;
    private final String cvv;
    private final String account;
    private final LocalDate validity;
    private final CardState state;
    private BigDecimal balance;

    public Card(String id, PaymentSystem paymentSystem, String bankId, int control, String cvv, String account, LocalDate validity, CardState state) {
        this.id = id;
        this.paymentSystem = paymentSystem;
        this.bankId = bankId;
        this.control = control;
        this.cvv = cvv;
        this.account = account;
        this.validity = validity;
        this.balance = BigDecimal.ZERO;
        this.state = state;
    }

    public CardState getState() {
        return state;
    }

    public Card activate() {
        if (state.equals(CardState.ACTIVE)) {
            return this;
        }
        if (state != CardState.CLOSED) {
            return new Card(id, paymentSystem, bankId, control, cvv, account, validity, CardState.ACTIVE);
        }
        throw new IllegalStateException("Card is already closed.");
    }

    public Card close() {
        if (state != CardState.PROCESS) {
            return new Card(id, paymentSystem, bankId, control, cvv, account, validity, CardState.CLOSED);
        }
        throw new IllegalStateException("Card has not been created yet.");
    }

    public String getId() {
        return id;
    }

    public PaymentSystem getPaymentSystem() {
        return paymentSystem;
    }

    public String getBankId() {
        return bankId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void plusBalance(BigDecimal balance) {
        this.balance = this.balance.add(balance);
    }

    public int getControl() {
        return control;
    }

    public String getCvv() {
        return cvv;
    }

    public String getAccount() {
        return account;
    }

    public LocalDate getValidity() {
        return validity;
    }

    public String getCardNum() {
        return paymentSystem.getCode() + bankId + id + control;
    }
}
