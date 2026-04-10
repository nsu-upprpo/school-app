package com.example.schoolapp.model;

public class ChildDto {
    private String id;
    private String firstName;
    private String lastName;
    private String patronymic;
    private String birthDate;
    private String branchId;

    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPatronymic() { return patronymic; }
    public String getBirthDate() { return birthDate; }
    public String getBranchId() { return branchId; }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) sb.append(firstName);
        if (lastName != null && !lastName.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(lastName);
        }
        if (patronymic != null && !patronymic.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(patronymic);
        }
        return sb.toString().trim();
    }
}
