package com.github.nsu_upprpo.school_app.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CreateChildRequest {

    @NotBlank(message = "Имя ребёнка обязательно")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Фамилия ребёнка обязательна")
    @Size(max = 50)
    private String lastName;

    @Size(max = 50)
    private String patronymic;

    private LocalDate birthDate;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, message = "Пароль должен быть не менее 8 символов")
    private String password;

}
