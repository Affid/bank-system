package com.sberstart.affid.banksystem.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sberstart.affid.banksystem.model.Account;
import com.sberstart.affid.banksystem.model.Client;

import java.io.IOException;

public class ClientSerializer extends StdSerializer<Client> {

    public ClientSerializer() {
        this(Client.class);
    }

    protected ClientSerializer(Class<Client> t) {
        super(t);
    }

    @Override
    public void serialize(Client client, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("lastName", client.getPassport().getLastName());
        jsonGenerator.writeStringField("firstName", client.getPassport().getFirstName());
        jsonGenerator.writeStringField("secondName", client.getPassport().getSecondName());
        jsonGenerator.writeStringField("phone", client.getPhone());
        jsonGenerator.writeArrayFieldStart("accounts");
        for (Account account : client.getAccounts()) {
            jsonGenerator.writeString(account.getAccount());
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }
}
