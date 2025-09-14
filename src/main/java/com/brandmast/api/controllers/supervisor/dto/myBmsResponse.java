package com.brandmast.api.controllers.supervisor.dto;

import java.util.List;

public class myBmsResponse {

    public static class Akcja_typ {
        //Sklep gdzie jest akcja
        private int shop_id;
        private String shop_name;
        private String shop_address;

        //Data i godziny akcji
        private int action_id;
        private String action_date;
        private String action_system_start;
        private String action_system_end;

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

    }

    private int bm_id;
    private String bm_login;
    private String bm_imie;
    private String bm_nazwisko;
    private String area_name;
    private String team_type;
    private int supervisor_id;
    private List<Akcja_typ> actions;

    public int getBm_id() {
        return bm_id;
    }
    public void setBm_id(int bm_id) {
        this.bm_id = bm_id;
    }

    public String getBm_login() {
        return bm_login;
    }
    public void setBm_login(String bm_login) {
        this.bm_login = bm_login;
    }

    public String getBm_imie() {
        return bm_imie;
    }
    public void setBm_imie(String bm_imie) {
        this.bm_imie = bm_imie;
    }

    public String getBm_nazwisko() {
        return bm_nazwisko;
    }
    public void setBm_nazwisko(String bm_nazwisko) {
        this.bm_nazwisko = bm_nazwisko;
    }

    public String getArea_name() {
        return area_name;
    }
    public void setArea_name(String area_name) {
        this.area_name = area_name;
    }

    public String getTeam_type() {
        return team_type;
    }
    public void setTeam_type(String team_type) {
        this.team_type = team_type;
    }

    public int getSupervisor_id() {
        return supervisor_id;
    }
    public void setSupervisor_id(int supervisor_id) {
        this.supervisor_id = supervisor_id;
    }

    public List<Akcja_typ> getActions() {
        return actions;
    }
    public void setActions(List<Akcja_typ> actions) {
        this.actions = actions;
    }
}
