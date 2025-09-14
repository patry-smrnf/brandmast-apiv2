package com.brandmast.api.controllers.supervisor;

import com.brandmast.api.Security;
import com.brandmast.api.controllers.brandmaster.dto.ActionsResponse;
import com.brandmast.api.controllers.supervisor.dto.myBmsResponse;
import com.brandmast.api.entity.Akcja;
import com.brandmast.api.entity.Brandmaster;
import com.brandmast.api.entity.Supervisor;
import com.brandmast.api.entity.Team;
import com.brandmast.api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sv")
public class BrandmasterController {
    @Autowired
    private BrandmasterRepository brandmasterRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SupervisorRepository supervisorRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private AkcjaRepository akcjaRepository;


    @GetMapping("/myBms")
    public ResponseEntity<?> myBms(@CookieValue(value = "Authtoken", required = true) String authToken, @RequestParam(value = "month", required = false) Integer month) {
        Security security_response = Security.check_security_SV(authToken, userRepository, supervisorRepository);
        if (!security_response.success) {
            return new ResponseEntity<>(security_response.message, HttpStatus.UNAUTHORIZED);
        }

        Supervisor sv_Object = (Supervisor) security_response.data;

        Optional<Team> optionalTeam = teamRepository.findBySupervisor(sv_Object);
        if(!optionalTeam.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\":\"Problem with finding your team\"}");
        }

        List<Brandmaster> brandmasters = brandmasterRepository.findByTeam(optionalTeam.get());

        List<myBmsResponse> resultJson = new ArrayList<>();

        for(var brandmaster_single : brandmasters) {
            myBmsResponse myBmsResponse_temporary = new myBmsResponse();

            List<Akcja> actions = akcjaRepository.findByBrandmasterAndMonthAndActionID(brandmaster_single, month, null);

            List<myBmsResponse.Akcja_typ> akcja_typs = new ArrayList<>();

            for(Akcja action : actions) {
                myBmsResponse.Akcja_typ akcja_typ_temp = new myBmsResponse.Akcja_typ();

                akcja_typ_temp.setAction_date(action.getData().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                akcja_typ_temp.setAction_system_start(action.getPlannedStart().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                akcja_typ_temp.setAction_system_end(action.getPlannedStop().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                akcja_typ_temp.setShop_id(action.getShop().getIdShop());
                akcja_typ_temp.setShop_name(action.getShop().getName());
                akcja_typ_temp.setShop_id(action.getShop().getIdShop());
                akcja_typ_temp.setAction_id(action.getIdAkcja());

                akcja_typs.add(akcja_typ_temp);
            }

            myBmsResponse_temporary.setActions(akcja_typs);

            myBmsResponse_temporary.setBm_id(brandmaster_single.getIdBm());
            myBmsResponse_temporary.setBm_imie(brandmaster_single.getUser().getImie());
            myBmsResponse_temporary.setBm_nazwisko(brandmaster_single.getUser().getNazwisko());
            myBmsResponse_temporary.setBm_login(brandmaster_single.getUser().getLogin());

            myBmsResponse_temporary.setTeam_type(brandmaster_single.getTeam().getType());
            myBmsResponse_temporary.setSupervisor_id(brandmaster_single.getTeam().getSupervisor().getIdSv());
            myBmsResponse_temporary.setArea_name(brandmaster_single.getTeam().getArea().getArea_name());

            resultJson.add(myBmsResponse_temporary);
        }

        return ResponseEntity.ok(resultJson);
    }
}
