package com.brandmast.api.repository;

import com.brandmast.api.entity.Shop;
import com.brandmast.api.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShopRepository  extends JpaRepository<Shop, Integer> {
    List<Shop> findShopByTeam(Team team);
}
