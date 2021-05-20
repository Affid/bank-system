package com.sberstart.affid.banksystem.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Account {
    private final String balancingAccount;

    private final Currency currency;

    private final String division;

    private final int control;

    private final String personalAcc;

    private final String owner;

    private final long id;
    private final ArrayList<Card> cards;
    private BigDecimal balance;

    public Account(String balancingAccount, Currency currency, String division,
                   int control, String personalAcc, String owner, long id,
                   BigDecimal balance, ArrayList<Card> cards) {
        this.balancingAccount = balancingAccount;
        this.currency = currency;
        this.division = division;
        this.personalAcc = personalAcc;
        this.owner = owner;
        this.id = id;
        this.balance = balance;
        this.control = control;
        this.cards = cards;
    }

    public void addBalance(BigDecimal val) {
        balance = balance.add(val);
    }

    public void minusBalance(BigDecimal val) {
        balance = balance.subtract(val);
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getOwner() {
        return owner;
    }

    public long getId() {
        return id;
    }

    public String getAccount() {
        return balancingAccount + currency.getCode() + control + division + personalAcc;
    }

    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }
}
