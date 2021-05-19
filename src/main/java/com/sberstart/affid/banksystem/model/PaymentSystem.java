package com.sberstart.affid.banksystem.model;

public enum PaymentSystem {
    MIR(2), AMERICAN_EXPRESS(3), VISA(4), MASTERCARD(5);

    private final int code;

    PaymentSystem(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static PaymentSystem of(int code){
        for(PaymentSystem system: values()){
            if(system.code == code){
                return system;
            }
        }
        throw new IllegalArgumentException("Constant not found");
    }
}
