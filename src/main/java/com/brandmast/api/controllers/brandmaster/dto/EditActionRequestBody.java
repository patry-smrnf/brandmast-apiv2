package com.brandmast.api.controllers.brandmaster.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;


public class EditActionRequestBody {
    @NotNull(message = "id_action is required")
    @Positive(message = "id_action must be positive")
    private Integer id_action;

    @NotBlank(message = "Date is required")
    private String action_date;

    @JsonFormat(pattern = "HH:mm:ss")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$", message = "Invalid time format, expected HH:mm:ss")
    private String action_system_start;

    @JsonFormat(pattern = "HH:mm:ss")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$", message = "Invalid time format, expected HH:mm:ss")
    private String action_system_end;

    @JsonFormat(pattern = "HH:mm:ss")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$", message = "Invalid time format, expected HH:mm:ss")
    private String action_real_start;

    @JsonFormat(pattern = "HH:mm:ss")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$", message = "Invalid time format, expected HH:mm:ss")
    private String action_real_end;

    @NotBlank(message = "Shop address is required")
    private String shop_address;

    private Boolean szkolenie;

    public int getId_action() {
        return id_action;
    }
    public void setId_action(int id_action) {
        this.id_action = id_action;
    }

    public String getAction_date() {
        return action_date;
    }
    public void setAction_date(String action_date) {
        this.action_date = action_date;
    }

    public String getAction_system_start() {
        return action_system_start;
    }
    public void setAction_system_start(String action_system_start) {
        this.action_system_start = action_system_start;
    }

    public String getAction_system_end() {
        return action_system_end;
    }
    public void setAction_system_end(String action_system_end) {
        this.action_system_end = action_system_end;
    }

    public String getAction_real_start() {
        return action_real_start;
    }
    public void setAction_real_start(String action_real_start) {
        this.action_real_start = action_real_start;
    }

    public String getAction_real_end() {
        return action_real_end;
    }
    public void setAction_real_end(String action_real_end) {
        this.action_real_end = action_real_end;
    }

    public String getShop_address() {
        return shop_address;
    }
    public void setShop_address(String shop_address) {
        this.shop_address = shop_address;
    }

    public Boolean getSzkolenie() {
        return szkolenie;
    }
    public void setSzkolenie(Boolean szkolenie) {
        this.szkolenie = szkolenie;
    }
}
