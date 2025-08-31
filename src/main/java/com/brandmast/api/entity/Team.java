package com.brandmast.api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "team")
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_team")
    private Integer idTeam;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sv", insertable = false, updatable = false)
    private Supervisor supervisor;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_area", insertable = false, updatable = false)
    private Area area;

    @Column(name = "type")
    private String type;

    //Team ID
    public Integer getIdTeam() {
        return idTeam;
    }
    public void setIdTeam(Integer idTeam) {
        this.idTeam = idTeam;
    }

    //Team Supervisor
    public Supervisor getSupervisor() {
        return supervisor;
    }
    public void setSupervisor(Supervisor supervisor) {
        this.supervisor = supervisor;
    }

    //Team Area
    public Area getArea() {
        return area;
    }
    public void setArea(Area area) {
        this.area = area;
    }

    //Team Type
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
