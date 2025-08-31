package com.brandmast.api.repository;

import com.brandmast.api.entity.Admin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Integer> {
    Optional<Admin> findByUser_IdUser(Integer idUser);
}
