package com.sberstart.affid.banksystem.json.schemas;

public class ChangeCardStateBody {
    private String id;

    private int state;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
