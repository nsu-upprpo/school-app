package com.github.nsu_upprpo.school_app.model;

import java.util.List;

public class ChildDto {
    private String id;
    private String firstName;
    private String lastName;
    private String patronymic;
    private String birthDate;
    private List<GroupDto> groups;

    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPatronymic() { return patronymic; }
    public String getBirthDate() { return birthDate; }
    public List<GroupDto> getGroups() { return groups; }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();

        if (lastName != null && !lastName.isEmpty()) sb.append(lastName);

        if (firstName != null && !firstName.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(firstName);
        }

        if (patronymic != null && !patronymic.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(patronymic);
        }

        return sb.toString().trim();
    }
}