package com.brandmast.api.controllers.brandmaster;

import com.brandmast.api.Security;
import com.brandmast.api.controllers.brandmaster.dto.ActionsResponse;
import com.brandmast.api.controllers.brandmaster.dto.AddActionRequestBody;
import com.brandmast.api.controllers.brandmaster.dto.DelActionRequestBody;
import com.brandmast.api.controllers.brandmaster.dto.EditActionRequestBody;
import com.brandmast.api.entity.*;
import com.brandmast.api.repository.*;

import jakarta.validation.Valid;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bm")
public class ActionController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BrandmasterRepository brandmasterRepository;
    @Autowired
    private AkcjaRepository akcjaRepository;
    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private TeamRepository teamRepository;

    @GetMapping("/actions")
    public ResponseEntity<?> getActions(@CookieValue(value = "Authtoken", required = true) String authToken, @RequestParam(value = "month", required = false) Integer month, @RequestParam(value = "actionID", required = false) Integer actionID) {

        Security security_response = Security.check_security_BM(authToken, userRepository, brandmasterRepository);

        if (!security_response.success) {
            return new ResponseEntity<>(security_response.message, HttpStatus.UNAUTHORIZED);
        }

        Brandmaster bm_Object = (Brandmaster) security_response.data;

        // Fetch filtered actions directly from DB
        List<Akcja> actions = akcjaRepository.findByBrandmasterAndMonthAndActionID(bm_Object, month, actionID);

        List<ActionsResponse> resultJson = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Akcja action : actions) {
            ActionsResponse res = new ActionsResponse();
            res.setAction_id(action.getIdAkcja());
            res.setShop_address(action.getShop().getAddress());
            res.setShop_name(action.getShop().getName());
            res.setShop_id(action.getShop().getIdShop());
            res.setAction_date(action.getData().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            res.setAction_system_start(action.getPlannedStart().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            res.setAction_system_end(action.getPlannedStop().format(DateTimeFormatter.ofPattern("HH:mm:ss"))); // assuming you meant PlannedEnd

            res.setAction_real_start(action.getRealStart() != null ? action.getRealStart().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : null);
            res.setAction_real_end(action.getRealStop() != null ? action.getRealStop().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : null);
            res.setSzkolenie(action.isSzkolenie());
            res.setPast(action.getData().isBefore(today));

            ActionsResponse.Cta cta = new ActionsResponse.Cta();
            cta.setLabel("Edytuj");
            cta.setHref("/EditAkcja?id=" + action.getIdAkcja());
            res.setCta(cta);

            resultJson.add(res);
        }

        return ResponseEntity.ok(resultJson);
    }

    @PostMapping("/editAction")
    public ResponseEntity<?> editAction(@Valid @RequestBody EditActionRequestBody request, @CookieValue(value = "Authtoken", required = true) String authToken) {
        Security security_response = Security.check_security_BM(authToken, userRepository, brandmasterRepository);
        if (!security_response.success) {
            return new ResponseEntity<>(security_response.message, HttpStatus.UNAUTHORIZED);
        }

        Brandmaster bm_Object = (Brandmaster) security_response.data;

        Optional<Akcja> optional_akcja = akcjaRepository.findById(request.getId_action());
        if(!optional_akcja.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\":\"Action not found\"}");
        }

        Akcja akcja_to_edit = optional_akcja.get();
        if(akcja_to_edit.getBrandmaster() != bm_Object) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\":\"Akcja nie nalezy do ciebie :// \"}");
        }

        //Przetwarzanie danych frontend
        LocalDate action_date = LocalDate.parse(request.getAction_date(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        LocalDateTime action_startSys = LocalDateTime.of(action_date, LocalTime.parse(request.getAction_system_start(), DateTimeFormatter.ofPattern("HH:mm:ss")));
        LocalDateTime action_stopSys = LocalDateTime.of(action_date, LocalTime.parse(request.getAction_system_end(), DateTimeFormatter.ofPattern("HH:mm:ss")));

        LocalDateTime action_startRel = null;
        LocalDateTime action_stopRel = null;

        if(request.getAction_real_start() != null) {
            action_startRel = LocalDateTime.of(action_date, LocalTime.parse(request.getAction_real_start(), DateTimeFormatter.ofPattern("HH:mm:ss")));
        }
        if(request.getAction_real_end() != null) {
            action_stopRel = LocalDateTime.of(action_date, LocalTime.parse(request.getAction_real_end(), DateTimeFormatter.ofPattern("HH:mm:ss")));
        }

        //Sprawdzenie czy dane sa spojne logicznie, czyli czy stop jest po start itd
        if(action_startSys.isAfter(action_stopSys)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\":\"Wpierw sie zaczyna, potem konczy ( Systemowe )\"}");
        }
        if (action_startRel != null && action_stopRel != null) {
            if (action_startRel.isAfter(action_stopRel)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"message\":\"Wpierw sie zaczyna, potem konczy ( Realne )\"}");
            }
        }

        //Znajdywanie sklepu poprzez podany przez user adres sklepu
        Optional<Shop> optional_shop = shopRepository.findByAddress(request.getShop_address());
        if(!optional_shop.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\":\"Nie istnieje sklep o takim adresie w bazie\"}");
        }
        Shop shop_found = optional_shop.get();

        if(!akcjaRepository.findOverlappingPlanned(akcja_to_edit.getBrandmaster().getIdBm(), akcja_to_edit.getIdAkcja(), action_startSys, action_stopSys).isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\":\"Masz juz akcje zaplanowana w tych godzinach tego samego dnia mordzia\"}");
        }
        if(action_startRel != null && action_stopRel != null) {
            if(!akcjaRepository.findOverlappingReal(akcja_to_edit.getBrandmaster().getIdBm(), akcja_to_edit.getIdAkcja(), action_startRel, action_stopRel).isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\":\"Masz juz akcje ktora realnie konczysz w tych godzinach tego samego dnia mordzia\"}");
            }
        }

        akcja_to_edit.setData(action_date);
        akcja_to_edit.setPlannedStart(action_startSys);
        akcja_to_edit.setPlannedStop(action_stopSys);
        akcja_to_edit.setRealStart(action_startRel);
        akcja_to_edit.setRealStop(action_stopRel);
        akcja_to_edit.setSzkolenie(request.getSzkolenie());
        akcja_to_edit.setShop(shop_found);
        akcjaRepository.save(akcja_to_edit);

        return ResponseEntity.ok("{\"message\":\"Akcja updated successfully\"}");
    }

    @PostMapping("/addAction")
    public ResponseEntity<?> addAction(@Valid @RequestBody AddActionRequestBody request, @CookieValue(value = "Authtoken", required = true) String authToken) {
        Security security_response = Security.check_security_BM(authToken, userRepository, brandmasterRepository);
        if (!security_response.success) {
            return new ResponseEntity<>(security_response.message, HttpStatus.UNAUTHORIZED);
        }

        Brandmaster bm_Object = (Brandmaster) security_response.data;

        Optional<Team> optionalTeam = teamRepository.findById(bm_Object.getTeam().getIdTeam());
        if(!optionalTeam.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\":\"Nie udalo sie znalezc twojego teamu\"}");
        }
        Team team_found = optionalTeam.get();

        //Przetwarzanie danych frontend
        LocalDate action_date = LocalDate.parse(request.getAction_date(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        LocalDateTime action_startSys = LocalDateTime.of(action_date, LocalTime.parse(request.getAction_system_start(), DateTimeFormatter.ofPattern("HH:mm:ss")));
        LocalDateTime action_stopSys = LocalDateTime.of(action_date, LocalTime.parse(request.getAction_system_end(), DateTimeFormatter.ofPattern("HH:mm:ss")));


        //Znajdywanie sklepu poprzez podany przez user adres sklepu
        Optional<Shop> optional_shop = shopRepository.findByAddress(request.getShop_address());
        if(!optional_shop.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\":\"Nie istnieje sklep o takim adresie w bazie\"}");
        }
        Shop shop_found = optional_shop.get();

        if(!akcjaRepository.findOverlappingPlanned(bm_Object.getIdBm(), 0, action_startSys, action_stopSys).isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\":\"Masz juz akcje zaplanowana w tych godzinach tego samego dnia mordzia\"}");
        }

        Akcja akcja_to_Create = new Akcja();
        akcja_to_Create.setBrandmaster(bm_Object);
        akcja_to_Create.setShop(shop_found);
        akcja_to_Create.setData(action_date);
        akcja_to_Create.setPlannedStart(action_startSys);
        akcja_to_Create.setPlannedStop(action_stopSys);
        akcja_to_Create.setTeam(team_found);

        akcjaRepository.save(akcja_to_Create);

        return ResponseEntity.ok("{\"message\":\"Akcja created successfully\"}");
    }

    @PostMapping("/delAction")
    public ResponseEntity<?> delAction(@Valid @RequestBody DelActionRequestBody request, @CookieValue(value = "Authtoken", required = true) String authToken) {
        Security security_response = Security.check_security_BM(authToken, userRepository, brandmasterRepository);
        if (!security_response.success) {
            return new ResponseEntity<>(security_response.message, HttpStatus.UNAUTHORIZED);
        }

        Brandmaster bm_Object = (Brandmaster) security_response.data;
        Optional<Akcja> optional_akcja = akcjaRepository.findById(request.getId_action());
        if(!optional_akcja.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\":\"Action not found\"}");
        }

        Akcja akcja_to_edit = optional_akcja.get();
        if(akcja_to_edit.getBrandmaster() != bm_Object) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\":\"Akcja nie nalezy do ciebie :// \"}");
        }

        akcjaRepository.delete(akcja_to_edit);
        return ResponseEntity.ok("{\"message\":\"Action successfully deleted\"}");

    }

}
