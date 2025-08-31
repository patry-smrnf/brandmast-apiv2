package com.brandmast.api.repository;

import com.brandmast.api.entity.Supervisor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface SupervisorRepository extends JpaRepository<Supervisor, Integer> {
    Optional<Supervisor> findByUser_IdUser(Integer idUser);
}
