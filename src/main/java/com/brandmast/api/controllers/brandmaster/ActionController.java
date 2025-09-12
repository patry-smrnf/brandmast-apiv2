package com.brandmast.api.controllers.brandmaster;

import com.brandmast.api.Security;
import com.brandmast.api.controllers.brandmaster.dto.ActionsResponse;
import com.brandmast.api.entity.Akcja;
import com.brandmast.api.entity.Brandmaster;
import com.brandmast.api.entity.User;
import com.brandmast.api.repository.AkcjaRepository;
import com.brandmast.api.repository.BrandmasterRepository;
import com.brandmast.api.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/bm")
public class ActionController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BrandmasterRepository brandmasterRepository;
    @Autowired
    private AkcjaRepository akcjaRepository;

    @GetMapping("/actions")
    public ResponseEntity<?> getActions(
            @CookieValue(value = "Authtoken", required = true) String authToken,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "actionID", required = false) Integer actionID) {

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

}
