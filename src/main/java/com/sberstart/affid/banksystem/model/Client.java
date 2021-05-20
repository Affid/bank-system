package com.sberstart.affid.banksystem.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Client {
    private final long id;

    private final Passport passport;

    private final String phone;

    private final ArrayList<Account> accounts;

    public Client(long id, Passport passport, String phone, ArrayList<Account> accounts) {
        this.id = id;
        this.passport = passport;
        this.phone = phone;
        this.accounts = accounts;
    }

    public boolean addAccount(Account account) {
        return accounts.add(account);
    }

    public boolean removeAccount(Account account) {
        return accounts.remove(account);
    }

    public String getPhone() {
        return phone;
    }

    public List<Account> getAccounts() {
        return new ArrayList<>(accounts);
    }

    public Client changePhone(String phone) {
        return new Client(id, passport, phone, accounts);
    }

    public long getId() {
        return id;
    }

    public Passport getPassport() {
        return passport;
    }

    public Client changePassport(Passport passport) {
        if (passport != null) {
            return new Client(id, passport, phone, accounts);
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return id == client.id && passport.equals(client.passport) && phone.equals(client.phone) && accounts.equals(client.accounts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, passport, phone, accounts);
    }
}
