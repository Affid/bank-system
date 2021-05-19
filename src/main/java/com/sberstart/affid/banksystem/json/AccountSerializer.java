package com.sberstart.affid.banksystem.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sberstart.affid.banksystem.model.Account;
import com.sberstart.affid.banksystem.model.Card;

import java.io.IOException;

public class AccountSerializer extends StdSerializer<Account> {

    public AccountSerializer(){
        this(Account.class);
    }

    protected AccountSerializer(Class<Account> t) {
        super(t);
    }

    @Override
    public void serialize(Account account, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("num", account.getAccount());
        jsonGenerator.writeStringField("currency",account.getCurrency().toString());
        jsonGenerator.writeStringField("owner", account.getOwner());
        jsonGenerator.writeNumberField("balance", account.getBalance());
        jsonGenerator.writeArrayFieldStart("cards");
        for(Card card: account.getCards()){
            jsonGenerator.writeString(card.getCardNum());
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }
}
