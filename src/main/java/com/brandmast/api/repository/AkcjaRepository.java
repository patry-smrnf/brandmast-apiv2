package com.brandmast.api.repository;

import com.brandmast.api.entity.Akcja;
import com.brandmast.api.entity.Brandmaster;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AkcjaRepository extends JpaRepository<Akcja, Integer> {
    @Query("SELECT a FROM Akcja a " +
            "LEFT JOIN FETCH a.shop s " +
            "LEFT JOIN FETCH a.brandmaster bm " +
            "WHERE bm IN :brandmasters " +
            "AND (:month IS NULL OR MONTH(a.data) = :month)")
    List<Akcja> findByBrandmasterInAndMonth(@Param("brandmasters") List<Brandmaster> brandmasters, @Param("month") Integer month);

    List<Akcja> findByBrandmaster(Brandmaster bm);

    List<Akcja> findByBrandmasterOrderByDataAsc(Brandmaster bm);

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
    List<Akcja> findByBrandmasterAndMonthAndActionID(@Param("bm") Brandmaster bm, @Param("month") Integer month, @Param("actionID") Integer actionID);

    @Query("""
        SELECT a FROM Akcja a
        WHERE a.brandmaster.idBm = :bmId
          AND a.idAkcja <> :akcjaId
          AND a.plannedStart < :end
          AND a.plannedStop > :start
    """)
    List<Akcja> findOverlappingPlanned(@Param("bmId") Integer bmId, @Param("akcjaId") Integer akcjaId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Check for overlaps in real time
    @Query("""
        SELECT a FROM Akcja a
        WHERE a.brandmaster.idBm = :bmId
          AND a.idAkcja <> :akcjaId
          AND a.realStart < :end
          AND a.realStop > :start
    """)
    List<Akcja> findOverlappingReal(@Param("bmId") Integer bmId, @Param("akcjaId") Integer akcjaId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Transactional
    @Modifying
    @Query("DELETE FROM Akcja a WHERE a.shop.idShop = :shopId")
    void deleteByShopId(@Param("shopId") Integer shopId);

}

