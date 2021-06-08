package com.sberstart.affid.banksystem.json.schemas;

public class CreateAccountBody {

    private String account;
    private String owner;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
