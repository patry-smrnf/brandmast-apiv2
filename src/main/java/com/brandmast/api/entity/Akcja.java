package com.brandmast.api.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "akcje")
public class Akcja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_akcja", updatable = false, nullable = false)
    private Integer idAkcja;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_shop", nullable = true)
    private Shop shop;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bm", nullable = true)
    private Brandmaster brandmaster;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_team", nullable = true)
    private Team team;

    @Column(name = "data")
    private LocalDate data;

    @Column(name = "planned_start")
    private LocalDateTime plannedStart;

    @Column(name = "planned_stop")
    private LocalDateTime plannedStop;

    @Column(name = "real_start")
    private LocalDateTime realStart;

    @Column(name = "real_stop")
    private LocalDateTime realStop;

    @Column(name = "szkolenie")
    private boolean szkolenie;

    @Column(name = "czas_wpisania")
    private OffsetDateTime czasWpisania;

    public Integer getIdAkcja() {
        return idAkcja;
    }
    public void setIdAkcja(Integer idAkcja) {
        this.idAkcja = idAkcja;
    }

    public boolean isSzkolenie() {
        return szkolenie;
    }
    public void setSzkolenie(boolean szkolenie) {
        this.szkolenie = szkolenie;
    }

    public Shop getShop() {
        return shop;
    }
    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public Brandmaster getBrandmaster() {
        return brandmaster;
    }
    public void setBrandmaster(Brandmaster brandmaster) {
        this.brandmaster = brandmaster;
    }

    public Team getTeam() {
        return team;
    }
    public void setTeam(Team team) {
        this.team = team;
    }

    public LocalDate getData() {
        return data;
    }
    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalDateTime getPlannedStart() {
        return plannedStart;
    }
    public void setPlannedStart(LocalDateTime plannedStart) {
        this.plannedStart = plannedStart;
    }

    public LocalDateTime getPlannedStop() {
        return plannedStop;
    }
    public void setPlannedStop(LocalDateTime plannedStop) {
        this.plannedStop = plannedStop;
    }

    public LocalDateTime getRealStart() {
        return realStart;
    }
    public void setRealStart(LocalDateTime realStart) {
        this.realStart = realStart;
    }

    public LocalDateTime getRealStop() {
        return realStop;
    }
    public void setRealStop(LocalDateTime realStop) {
        this.realStop = realStop;
    }

    public OffsetDateTime getCzasWpisania() {
        return czasWpisania;
    }
    public void setCzasWpisania(OffsetDateTime czasWpisania) {
        this.czasWpisania = czasWpisania;
    }

    public static String convert_time(OffsetDateTime utcTimestamp) {
        // Replace with your country timezone, e.g. "Europe/Berln"
        ZoneId zone = ZoneId.of("Europe/Berlin");

        return utcTimestamp
                .atZoneSameInstant(zone)  // convert to local timezone
                .toLocalDateTime()        // strip zone info
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
