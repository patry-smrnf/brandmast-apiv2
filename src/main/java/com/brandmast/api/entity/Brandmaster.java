package com.brandmast.api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "brandmasters")
public class Brandmaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bm", updatable = false, nullable = false)
    private Integer idBm;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = true)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_team", nullable = false)
    private Team team;

    //Brandmaster ID
    public Integer getIdBm() {
        return idBm;
    }
    public void setIdBm(Integer idBm) {
        this.idBm = idBm;
    }

    //Brandmaster User
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    //Brandmaster Team
    public Team getTeam() {
        return team;
    }
    public void setTeam(Team team) {
        this.team = team;
    }
}
