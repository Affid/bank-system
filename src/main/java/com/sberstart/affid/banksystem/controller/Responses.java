package com.sberstart.affid.banksystem.controller;

public class Responses {
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String JSON_TYPE = "application/json";

    public static final String EMPTY = "Empty body";
    public static final String BAD_FORMATTING = "Non-well-formed content";
    public static final String BAD_CONTENT = "Invalid content fields";
    public static final String BAD_QUERY = "Invalid query parameters";
    public static final String ALREADY_EXISTS = "Already exists";
    public static final String RELATION_NOT_FOUND = "Relation %s doesn't exist";
    public static final String BAD_NUMBER = "Incorrect %s number";
    public static final String NOT_EXISTS = "%s doesn't exist";

    public static final String CARD_CLOSED = "Card closed";
    public static final String CARD_ALREADY_STATE = "Card already %s";

    public static final String FAIL = "Operation ended with fail";
}
