package com.sberstart.affid.banksystem.validation;

import com.sberstart.affid.banksystem.dao.DataSource;
import com.sberstart.affid.banksystem.json.schemas.CreateCardBody;
import com.sberstart.affid.banksystem.service.BankService;

import javax.management.InstanceAlreadyExistsException;
import javax.management.relation.InvalidRelationIdException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;

import static com.sberstart.affid.banksystem.controller.Responses.BAD_NUMBER;

public class CardBodyValidator implements Validator<CreateCardBody> {

    private static final DateTimeFormatter format = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd").toFormatter();


    @Override
    public boolean validate(CreateCardBody body) {
        LocalDate validity;
        try {
            TemporalAccessor accessor = format.parse(body.getValidity());
            validity = LocalDate.from(accessor);
        }
        catch (DateTimeParseException e){
            throw new IllegalStateException("INVALID VALIDITY FORMAT");
        }
        if (validity.isBefore(LocalDate.now())) {
            throw new IllegalStateException("INVALID VALIDITY");
        }
        if (body.getCvv() == null || body.getCvv().length() != 3 || !body.getCvv().matches("\\d{3}")) {
            throw new IllegalStateException("INVALID CVV");
        }
        if (!validateCardNum(body.getNumber())) {
            throw new IllegalStateException(String.format(BAD_NUMBER, "card"));
        }
        return AccountBodyValidator.validateNumber(body.getAccount());
    }

    public static boolean validateCardNum(String number) {
        if (number.length() != 16) {
            return false;
        }
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
            int digit = digits[digits.length - 1 - i];
            if (i % 2 != 0) {
                digit = 2 * digit;
                if (digit > 9) {
                    digit = digit - 9;
                }
            }
            sum = sum + digit;
        }
        return sum % 10 == 0;
    }
}
