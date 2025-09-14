package com.brandmast.api.controllers.general;

import com.brandmast.api.Security;
import com.brandmast.api.controllers.brandmaster.dto.DelActionRequestBody;
import com.brandmast.api.controllers.general.dto.AddShopRequestBody;
import com.brandmast.api.controllers.general.dto.DelShopRequestBody;
import com.brandmast.api.controllers.general.dto.ShopsResponse;
import com.brandmast.api.entity.Brandmaster;
import com.brandmast.api.entity.Shop;
import com.brandmast.api.entity.Supervisor;
import com.brandmast.api.entity.Team;
import com.brandmast.api.repository.*;
import com.brandmast.api.service.GeoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/general")
public class ShopController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BrandmasterRepository brandmasterRepository;
    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private SupervisorRepository supervisorRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private GeoService geoService;
    @Autowired
    private AkcjaRepository akcjaRepository;

    @GetMapping("/getAllShops")
    public ResponseEntity<?> getActions(@CookieValue(value = "Authtoken", required = true) String authToken) {
        Team team_general = null;

        Security security_response = Security.check_security_BM(authToken, userRepository, brandmasterRepository);

        if (!security_response.success) {

            Security security_sv = Security.check_security_SV(authToken, userRepository, supervisorRepository);
            if (!security_sv.success) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{message:\"Unauthorized\"}");
            }
            else {
                Supervisor supervisor_general = (Supervisor) security_sv.data;
                Optional<Team> team_opt = teamRepository.findBySupervisor(supervisor_general);
                if(!team_opt.isPresent()) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{message:\"I dunno tbh\"}");
                }
                team_general = team_opt.get();
            }
        }
        else {
            Brandmaster brandmaster_general = (Brandmaster) security_response.data;
            team_general = brandmaster_general.getTeam();
        }

        //Znajduje sklepy, ale tylko te z teamu hehe
        List<Shop> sklepy = shopRepository.findShopByTeam(team_general);

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

    @PostMapping("/delShop")
    public ResponseEntity<?> delShop(@Valid @RequestBody DelShopRequestBody request, @CookieValue(value = "Authtoken", required = true) String authToken) {
        Security security_response = Security.check_security_SV(authToken, userRepository, supervisorRepository);
        if (!security_response.success) {
            return new ResponseEntity<>(security_response.message, HttpStatus.UNAUTHORIZED);
        }
        Supervisor sv_Object = (Supervisor) security_response.data;

        Optional<Shop> optionalShop = shopRepository.findById(request.getId_shop());
        if(!optionalShop.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\":\"Shop not found. Report error\"}");
        }
        Shop final_shop = optionalShop.get();

        Optional<Team> supervisor_team = teamRepository.findBySupervisor(sv_Object);

        if(supervisor_team.get().getIdTeam() != final_shop.getTeam().getIdTeam()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\":\"Shop doesnt belong to your team\"}");
        }

        akcjaRepository.deleteByShopId(final_shop.getIdShop());
        shopRepository.delete(final_shop);
        return ResponseEntity.ok("{\"message\":\"Shop successfully deleted\"}");
    }

    @PostMapping("/addShop")
    public ResponseEntity<?> addShop(@Valid @RequestBody AddShopRequestBody request, @CookieValue(value = "Authtoken", required = true) String authToken) {
        Security security_response = Security.check_security_SV(authToken, userRepository, supervisorRepository);
        if (!security_response.success) {
            return new ResponseEntity<>(security_response.message, HttpStatus.UNAUTHORIZED);
        }
        Supervisor sv_Object = (Supervisor) security_response.data;

        Optional<Team> optionalTeam = teamRepository.findBySupervisor(sv_Object);
        if(!optionalTeam.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\":\"Team not found. Report error\"}");
        }

        if(shopRepository.findByAddress(request.getAddress()).isPresent()) {
            return ResponseEntity.status(403).body("{\"message\":\"Shop with address '" + request.getAddress() + "' already exists\"}");
        }

        Shop newShop = new Shop();
        newShop.setAddress(request.getAddress());
        newShop.setName(request.getName());
        newShop.setZipcode(request.getZipcode());
        newShop.setTeam(optionalTeam.get());

        Optional<GeoService.Coordinates> coordsOpt = geoService.getCoordinates(request.getAddress(), request.getZipcode()).block();

        coordsOpt.ifPresent(coords -> {
            newShop.setLat(coords.latitude());
            newShop.setLon(coords.longitude());
        });

        shopRepository.save(newShop);
        return ResponseEntity.ok("{\"message\":\"Shop with address: " + newShop.getAddress() + " successfully created\"}");
    }
}
