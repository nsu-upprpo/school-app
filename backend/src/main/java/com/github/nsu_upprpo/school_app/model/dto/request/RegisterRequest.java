package com.github.nsu_upprpo.school_app.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Имя обязательно")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    @Size(max = 50)
    private String lastName;

    @Size(max = 50)
    private String patronymic;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;

    @Size(max = 20)
    private String phone;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, message = "Пароль должен быть не менее 8 символов")
    private String password;

}
