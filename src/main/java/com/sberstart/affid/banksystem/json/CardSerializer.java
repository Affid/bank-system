package com.sberstart.affid.banksystem.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sberstart.affid.banksystem.model.Card;

import java.io.IOException;

public class CardSerializer extends StdSerializer<Card> {

    public CardSerializer() {
        this(Card.class);
    }

    protected CardSerializer(Class<Card> t) {
        super(t);
    }

    @Override
    public void serialize(Card card, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("number", card.getCardNum());
        jsonGenerator.writeStringField("state", card.getState().toString());
        jsonGenerator.writeNumberField("balance", card.getBalance());
        jsonGenerator.writeStringField("paySystem", card.getPaymentSystem().toString());
        jsonGenerator.writeEndObject();
    }
}
