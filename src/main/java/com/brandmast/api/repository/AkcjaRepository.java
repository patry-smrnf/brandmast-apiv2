package com.brandmast.api.repository;

import com.brandmast.api.entity.Akcja;
import com.brandmast.api.entity.Brandmaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AkcjaRepository extends JpaRepository<Akcja, Integer> {
    List<Akcja> findByBrandmaster(Brandmaster bm);

    // Filter by Brandmaster and month
    @Query("SELECT a FROM Akcja a WHERE a.brandmaster = :bm AND (:month IS NULL OR MONTH(a.data) = :month)")
    List<Akcja> findByBrandmasterAndMonth(@Param("bm") Brandmaster bm, @Param("month") Integer month);

    // Filter by Brandmaster and actionID
    @Query("SELECT a FROM Akcja a WHERE a.brandmaster = :bm AND (:actionID IS NULL OR a.idAkcja = :actionID)")
    List<Akcja> findByBrandmasterAndActionID(@Param("bm") Brandmaster bm, @Param("actionID") Integer actionID);

    // Filter by both month and actionID
    @Query("SELECT a FROM Akcja a WHERE a.brandmaster = :bm " +
            "AND (:month IS NULL OR MONTH(a.data) = :month) " +
            "AND (:actionID IS NULL OR a.idAkcja = :actionID)")
    List<Akcja> findByBrandmasterAndMonthAndActionID(@Param("bm") Brandmaster bm,
                                                     @Param("month") Integer month,
                                                     @Param("actionID") Integer actionID);
}

