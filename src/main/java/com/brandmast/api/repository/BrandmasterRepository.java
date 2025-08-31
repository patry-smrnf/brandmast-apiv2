package com.brandmast.api.repository;

import com.brandmast.api.entity.Brandmaster;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrandmasterRepository extends JpaRepository<Brandmaster, Integer> {
    Optional<Brandmaster> findByUser_IdUser(Integer idUser);
}
