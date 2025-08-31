package com.brandmast.api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "supervisors")
public class Supervisor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sv")
    private Integer idSv;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", insertable = false, updatable = false)
    private User user;

    //Supervisor ID
    public Integer getIdSv() {
        return idSv;
    }
    public void setIdSv(Integer idSv) {
        this.idSv = idSv;
    }

    //Supervisor User
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
}
