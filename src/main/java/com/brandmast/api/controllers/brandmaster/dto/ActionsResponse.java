package com.brandmast.api.controllers.brandmaster.dto;

public class ActionsResponse {

    //Sklep gdzie jest akcja
    private int shop_id;
    private String shop_name;
    private String shop_address;

    //Data i godziny akcji
    private int action_id;
    private String action_date;
    private String action_system_start;
    private String action_system_end;
    private String action_real_start;
    private String action_real_end;

    //Dodatkowe info
    private boolean szkolenie;
    private boolean isPast;

    //Dane do wyswieltania na stronie
    private Cta cta;

    //<---Stuff -->
    //Klasa do Przetrzymywania info o wyswietalniu tego
    public static class Cta {
        private String label;
        private String href;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }
    }

    //<---Settery & Gettery -->
    public int getShop_id() {
        return shop_id;
    }
    public void setShop_id(int shop_id) {
        this.shop_id = shop_id;
    }

    public String getShop_name() {
        return shop_name;
    }
    public void setShop_name(String shop_name) {
        this.shop_name = shop_name;
    }

    public String getShop_address() {
        return shop_address;
    }
    public void setShop_address(String shop_address) {
        this.shop_address = shop_address;
    }

    public int getAction_id() { return  action_id; }
    public void setAction_id(int action_id) { this.action_id = action_id; }

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

    public boolean isSzkolenie() {
        return szkolenie;
    }
    public void setSzkolenie(boolean szkolenie) {
        this.szkolenie = szkolenie;
    }

    public boolean isPast() { return isPast; }
    public void setPast(boolean past) { this.isPast = past; }

    public Cta getCta() {
        return cta;
    }
    public void setCta(Cta cta) {
        this.cta = cta;
    }
}
