package com.brandmast.api.controllers.general.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class DelShopRequestBody {
    @NotNull(message = "id_shop is required")
    @Positive(message = "id_shop must be positive")
    private Integer id_shop;

    public Integer getId_shop() {
        return id_shop;
    }
    public void setId_shop(Integer id_action) {
        this.id_shop = id_action;
    }
}
