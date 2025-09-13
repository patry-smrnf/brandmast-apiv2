package com.brandmast.api.controllers.brandmaster.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class DelActionRequestBody {
    @NotNull(message = "id_action is required")
    @Positive(message = "id_action must be positive")
    private Integer id_action;

    public Integer getId_action() {
        return id_action;
    }
    public void setId_action(Integer id_action) {
        this.id_action = id_action;
    }
}
