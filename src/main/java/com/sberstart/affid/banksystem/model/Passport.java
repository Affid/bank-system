package com.sberstart.affid.banksystem.model;

import java.time.LocalDate;

public class Passport {
    private final String id;

    private final String lastName;

    private final String firstName;

    private final String secondName;

    private final LocalDate birthDate;

    private final LocalDate issueDate;

    public Passport(String id, String lastName, String firstName, String secondName, LocalDate birthDate, LocalDate issueDate) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.secondName = secondName;
        this.birthDate = birthDate;
        this.issueDate = issueDate;
    }

    public String getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    @Override
    public String toString() {
        return "Passport{" +
                "id='" + id + '\'' +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", secondName='" + secondName + '\'' +
                ", birthDate=" + birthDate +
                ", issueDate=" + issueDate +
                '}';
    }
}
