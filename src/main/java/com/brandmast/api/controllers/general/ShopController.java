package com.brandmast.api.controllers.general;

import com.brandmast.api.Security;
import com.brandmast.api.controllers.general.dto.ShopsResponse;
import com.brandmast.api.entity.Brandmaster;
import com.brandmast.api.entity.Shop;
import com.brandmast.api.repository.BrandmasterRepository;
import com.brandmast.api.repository.ShopRepository;
import com.brandmast.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/general")
public class ShopController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BrandmasterRepository brandmasterRepository;
    @Autowired
    private ShopRepository shopRepository;

    @GetMapping("/getAllShops")
    public ResponseEntity<?> getActions(@CookieValue(value = "Authtoken", required = true) String authToken) {
        Security security_response = Security.check_security_BM(authToken, userRepository, brandmasterRepository);

        if (!security_response.success) {
            return new ResponseEntity<>(security_response.message, HttpStatus.UNAUTHORIZED);
        }

        Brandmaster bm_Object = (Brandmaster) security_response.data;

        //Znajduje sklepy, ale tylko te z teamu hehe
        List<Shop> sklepy = shopRepository.findShopByTeam(bm_Object.getTeam());

        List<ShopsResponse> resultJson = new ArrayList<>();

        for (Shop shop : sklepy) {
            ShopsResponse res = new ShopsResponse();

            res.setAddress(shop.getAddress());
            res.setName(shop.getName());
            res.setLat(shop.getLat());
            res.setLon(shop.getLon());
            res.setZipcode(shop.getZipcode());
            res.setId_shop(shop.getIdShop());

            resultJson.add(res);
        }

        return ResponseEntity.ok(resultJson);
    }
}
