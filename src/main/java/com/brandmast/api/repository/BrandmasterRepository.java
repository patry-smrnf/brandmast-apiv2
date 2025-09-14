package com.brandmast.api.repository;

import com.brandmast.api.entity.Brandmaster;

import com.brandmast.api.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BrandmasterRepository extends JpaRepository<Brandmaster, Integer> {

    @Query("SELECT bm FROM Brandmaster bm " +
            "JOIN FETCH bm.user u " +
            "JOIN FETCH bm.team t " +
            "LEFT JOIN FETCH t.area a " +
            "WHERE t = :team")
    List<Brandmaster> findByTeamWithUserAndTeamAndArea(@Param("team") Team team);

    Optional<Brandmaster> findByUser_IdUser(Integer idUser);
    List<Brandmaster> findByTeam(Team team);
}
