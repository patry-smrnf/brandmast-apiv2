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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    @Transactional(readOnly = true)
    public ResponseEntity<?> myBms(@CookieValue(value = "Authtoken", required = true) String authToken, @RequestParam(value = "month", required = false) Integer month) {

        Security security_response = Security.check_security_SV(authToken, userRepository, supervisorRepository);
        if (!security_response.success) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(security_response.message);
        }
        Supervisor sv = (Supervisor) security_response.data;

        Optional<Team> optionalTeam = teamRepository.findBySupervisor(sv);
        if (!optionalTeam.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "Problem with finding your team"));
        }
        Team team = optionalTeam.get();

        // fetch brandmasters with their user/team/area to avoid lazy loads
        List<Brandmaster> brandmasters = brandmasterRepository.findByTeamWithUserAndTeamAndArea(team);
        if (brandmasters.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        // fetch all akcje for those brandmasters in one query (avoids N+1)
        List<Akcja> akcje = akcjaRepository.findByBrandmasterInAndMonth(brandmasters, month);

        // group actions by brandmaster id for O(1) lookup while mapping
        Map<Integer, List<Akcja>> akcjeByBmId = akcje.stream()
                .filter(a -> a.getBrandmaster() != null)
                .collect(Collectors.groupingBy(a -> a.getBrandmaster().getIdBm()));

        // static formatters
        final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

        List<myBmsResponse> result = brandmasters.stream().map(bm -> {
            myBmsResponse dto = new myBmsResponse();
            dto.setBm_id(bm.getIdBm());
            dto.setBm_login(bm.getUser() != null ? bm.getUser().getLogin() : null);
            dto.setBm_imie(bm.getUser() != null ? bm.getUser().getImie() : null);
            dto.setBm_nazwisko(bm.getUser() != null ? bm.getUser().getNazwisko() : null);

            dto.setTeam_type(bm.getTeam() != null ? bm.getTeam().getType() : null);
            dto.setSupervisor_id(bm.getTeam() != null && bm.getTeam().getSupervisor() != null
                    ? bm.getTeam().getSupervisor().getIdSv() : 0);
            dto.setArea_name(bm.getTeam() != null && bm.getTeam().getArea() != null
                    ? bm.getTeam().getArea().getArea_name() : null);

            List<Akcja> bmAkcje = akcjeByBmId.getOrDefault(bm.getIdBm(), Collections.emptyList());
            List<myBmsResponse.Akcja_typ> akcja_typs = bmAkcje.stream().map(action -> {
                myBmsResponse.Akcja_typ at = new myBmsResponse.Akcja_typ();
                // date/time null-safe formatting
                if (action.getData() != null) {
                    at.setAction_date(action.getData().format(DATE_FMT));
                }
                if (action.getPlannedStart() != null) {
                    at.setAction_system_start(action.getPlannedStart().format(TIME_FMT));
                }
                if (action.getPlannedStop() != null) {
                    at.setAction_system_end(action.getPlannedStop().format(TIME_FMT));
                }
                if (action.getShop() != null) {
                    at.setShop_id(action.getShop().getIdShop());
                    at.setShop_name(action.getShop().getName());
                    at.setShop_address(action.getShop().getAddress()); // if exists
                }
                at.setAction_id(action.getIdAkcja() != null ? action.getIdAkcja() : 0);
                return at;
            }).collect(Collectors.toList());

            dto.setActions(akcja_typs);
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

}
