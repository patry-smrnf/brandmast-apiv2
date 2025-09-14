package com.brandmast.api.repository;

import com.brandmast.api.entity.Supervisor;
import com.brandmast.api.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Integer> {
    Optional<Team> findBySupervisor(Supervisor sv);
}
