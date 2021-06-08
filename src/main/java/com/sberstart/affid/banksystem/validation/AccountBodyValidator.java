package com.sberstart.affid.banksystem.validation;

import com.sberstart.affid.banksystem.controller.Responses;
import com.sberstart.affid.banksystem.json.schemas.CreateAccountBody;
import com.sberstart.affid.banksystem.model.Currency;

public class AccountBodyValidator implements Validator<CreateAccountBody> {

    private static final String RKC = "025";
    private static final int[] coeffs = {7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1};

    @Override
    public boolean validate(CreateAccountBody object) {
        if(!validateNumber(object.getAccount())){
            throw new IllegalStateException(String.format(Responses.BAD_NUMBER, "account"));
        }
        String currency = object.getAccount().substring(5,8);
        return Currency.contains(Integer.parseInt(currency));
    }

    public static boolean validateNumber(String number) {
        if (number.length() != 20) {
            return false;
        }
        number = String.join("", RKC, number);
        char[] digitChars = number.toCharArray();
        int[] digits = new int[digitChars.length];
        for (int i = 0; i < digitChars.length; i++) {
            if (!Character.isDigit(digitChars[i])) {
                return false;
            }
            digits[i] = digitChars[i] - '0';
        }
        int sum = 0;
        for (int i = 0; i < digits.length; i++) {
            sum += (digits[i] * coeffs[i]) % 10;
        }
        return sum % 10 == 0;
    }
}
