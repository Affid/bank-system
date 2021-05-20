package com.sberstart.affid.banksystem.model;

public enum CardState {
    ACTIVE("Карта активна"), PROCESS("Карта в процессе выпуска"),
    CLOSED("Карта закрыта");

    private final String description;

    CardState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
