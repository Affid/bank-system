package com.sberstart.affid.banksystem.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sberstart.affid.banksystem.model.Passport;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class PassportSerializer extends StdSerializer<Passport> {

    public PassportSerializer() {
        this(Passport.class);
    }

    protected PassportSerializer(Class<Passport> t) {
        super(t);
    }

    @Override
    public void serialize(Passport passport, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd").toFormatter();
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("id", passport.getId());
        jsonGenerator.writeStringField("lastName", passport.getLastName());
        jsonGenerator.writeStringField("firstName", passport.getFirstName());
        jsonGenerator.writeStringField("secondName", passport.getSecondName());
        jsonGenerator.writeStringField("issueDate", passport.getIssueDate().format(formatter));
        jsonGenerator.writeStringField("birthDate", passport.getBirthDate().format(formatter));
        jsonGenerator.writeEndObject();
    }
}
