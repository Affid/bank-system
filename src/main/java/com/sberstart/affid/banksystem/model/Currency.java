package com.sberstart.affid.banksystem.model;

public enum Currency {
    RUB(643, "Российский рубль"), EUR(978, "Евро"),
    USD(840, "Доллар США"), GBP(826, "Фунт стерлингов");

    private final int code;

    private final String name;

    Currency(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static Currency of(int code){
        for(Currency system: values()){
            if(system.code == code){
                return system;
            }
        }
        throw new IllegalArgumentException("Constant not found");
    }
}
