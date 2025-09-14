package com.brandmast.api.controllers.supervisor.dto;

public class AddBmRequestBody {
    public String login;
    public String Imie;
    public String Nazwisko;

    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }

    public String getImie() {
        return Imie;
    }
    public void setImie(String imie) {
        Imie = imie;
    }

    public String getNazwisko() {
        return Nazwisko;
    }
    public void setNazwisko(String nazwisko) {
        Nazwisko = nazwisko;
    }
}
