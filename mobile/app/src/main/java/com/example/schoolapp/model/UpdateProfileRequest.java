package com.example.schoolapp.model;

public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String patronymic;
    private String email;
    private String phone;

    public UpdateProfileRequest(String firstName, String lastName, String patronymic,
                                String email, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.patronymic = patronymic;
        this.email = email;
        this.phone = phone;
    }
}