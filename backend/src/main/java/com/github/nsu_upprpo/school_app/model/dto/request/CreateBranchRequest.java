package com.github.nsu_upprpo.school_app.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBranchRequest {

    @NotBlank(message = "Название обязательно")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Город обязателен")
    @Size(max = 100)
    private String city;

    @NotBlank(message = "Адрес обязателен")
    @Size(max = 255)
    private String address;

    @Size(max = 20)
    private String phone;

}
