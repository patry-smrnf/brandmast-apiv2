package com.brandmast.api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user", updatable = false, nullable = false)
    private Integer idUser;

    @Column(name = "login", nullable = false, unique = true, length = 20)
    private String login;

    @Column(name = "imie", nullable = true, unique = true, length = 20)
    private String imie;

    @Column(name = "nazwisko", nullable = true, unique = true, length = 20)
    private String nazwisko;

    //User ID
    public Integer getIdUser() {
        return idUser;
    }
    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    //User Login
    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }

    //User Imie
    public String getImie() {
        return imie;
    }
    public void setImie(String imie) {
        this.imie = imie;
    }

    //User Nazwisko
    public String getNazwisko() {
        return nazwisko;
    }
    public void setNazwisko(String nazwisko) {
        this.nazwisko = nazwisko;
    }
}
