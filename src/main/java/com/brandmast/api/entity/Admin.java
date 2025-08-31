package com.brandmast.api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "admin")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_admin", updatable = false, nullable = false)
    private Integer idAdmin;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", insertable = false, updatable = false)
    private User user;

    //Admin ID
    public Integer getIdAdmin() {
        return idAdmin;
    }
    public void setIdAdmin(Integer idAdmin) {
        this.idAdmin = idAdmin;
    }

    //Admin User
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
}
