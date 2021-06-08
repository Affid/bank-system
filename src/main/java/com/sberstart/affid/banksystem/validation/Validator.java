package com.sberstart.affid.banksystem.validation;

public interface Validator<T> {

    boolean validate(T object);

}
