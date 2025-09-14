package com.brandmast.api.controllers.supervisor;

import com.brandmast.api.Security;
import com.brandmast.api.entity.*;
import com.brandmast.api.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/sv")
public class ExcelController {

    private static final Logger log = LoggerFactory.getLogger(ExcelController.class);

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final UserRepository userRepository;
    private final SupervisorRepository supervisorRepository;
    private final TeamRepository teamRepository;
    private final BrandmasterRepository brandmasterRepository;
    private final AkcjaRepository akcjaRepository;
    private final ShopRepository shopRepository;

    public ExcelController(UserRepository userRepository,
                           SupervisorRepository supervisorRepository,
                           TeamRepository teamRepository,
                           BrandmasterRepository brandmasterRepository,
                           AkcjaRepository akcjaRepository,
                           ShopRepository shopRepository) {
        this.userRepository = userRepository;
        this.supervisorRepository = supervisorRepository;
        this.teamRepository = teamRepository;
        this.brandmasterRepository = brandmasterRepository;
        this.akcjaRepository = akcjaRepository;
        this.shopRepository = shopRepository;
    }

    /**
     * Build XLSX from bmDataList and the explicit list of day-strings to use as columns.
     */
    private ByteArrayInputStream exportBmDataToExcel(List<Map<String, Object>> bmDataList, List<String> dates) throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Dyspozycja");
            sheet.setDefaultColumnWidth(20);

            // Header style
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Address style
            CellStyle addressStyle = wb.createCellStyle();
            addressStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
            addressStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Date style (unused visually here but kept)
            CellStyle dateStyle = wb.createCellStyle();
            dateStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
            dateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Time style
            CellStyle timeStyle = wb.createCellStyle();
            timeStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
            timeStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // BM style
            CellStyle bmStyle = wb.createCellStyle();
            bmStyle.setFillForegroundColor(IndexedColors.TAN.getIndex());
            bmStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // 1) Header fixed columns
            Row hdr = sheet.createRow(0);
            String[] fixed = {"NR", "IMIE", "NAZWISKO", "PLH", "ŁĄCZNIE GODZIN"};
            for (int i = 0; i < fixed.length; i++) {
                Cell c = hdr.createCell(i);
                c.setCellValue(fixed[i]);
                c.setCellStyle(headerStyle);
            }

            // Day columns from provided dates list
            for (int i = 0; i < dates.size(); i++) {
                hdr.createCell(fixed.length + i).setCellValue(dates.get(i));
            }

            int rowIdx = 1;
            int nr = 1;

            for (Map<String, Object> bm : bmDataList) {
                String imie = Optional.ofNullable(bm.get("imie")).map(Object::toString).orElse("");
                String nazwisko = Optional.ofNullable(bm.get("nazwisko")).map(Object::toString).orElse("");
                String plh = Optional.ofNullable(bm.get("login")).map(Object::toString).orElse("");

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> akcjeAll = (List<Map<String, Object>>) bm.getOrDefault("akcje", Collections.emptyList());

                // Group actions by day string ("dd.MM.yyyy")
                Map<String, List<Map<String, Object>>> akcjeByDate = new HashMap<>();
                long totalMinutes = 0L;

                for (Map<String, Object> a : akcjeAll) {
                    String d = Optional.ofNullable(a.get("date")).map(Object::toString).orElse("");
                    akcjeByDate.computeIfAbsent(d, k -> new ArrayList<>()).add(a);

                    String startStr = Optional.ofNullable(a.get("start_sys")).map(Object::toString).orElse(null);
                    String stopStr = Optional.ofNullable(a.get("stop_sys")).map(Object::toString).orElse(null);
                    if (startStr != null && stopStr != null && !startStr.isBlank() && !stopStr.isBlank()) {
                        try {
                            LocalTime st = LocalTime.parse(startStr, TIME_FMT);
                            LocalTime en = LocalTime.parse(stopStr, TIME_FMT);
                            Duration dur = Duration.between(st, en);
                            if (dur.isNegative() || dur.isZero()) {
                                // assume stop on next day
                                dur = Duration.between(st, en.plusHours(24));
                            }
                            totalMinutes += dur.toMinutes();
                        } catch (Exception ex) {
                            log.warn("Failed to parse times '{}' - '{}': {}", startStr, stopStr, ex.getMessage());
                        }
                    }
                }

                String totalHours = String.format("%d:%02d", totalMinutes / 60, totalMinutes % 60);

                int maxCount = dates.stream()
                        .mapToInt(d -> akcjeByDate.getOrDefault(d, Collections.emptyList()).size())
                        .max()
                        .orElse(0);

                if (maxCount == 0) {
                    Row addrRow = sheet.createRow(rowIdx++);
                    Cell c0 = addrRow.createCell(0); c0.setCellValue(nr); c0.setCellStyle(bmStyle);
                    Cell c1 = addrRow.createCell(1); c1.setCellValue(imie); c1.setCellStyle(bmStyle);
                    Cell c2 = addrRow.createCell(2); c2.setCellValue(nazwisko); c2.setCellStyle(bmStyle);
                    Cell c3 = addrRow.createCell(3); c3.setCellValue(plh); c3.setCellStyle(bmStyle);
                    Cell c4 = addrRow.createCell(4); c4.setCellValue("0:00"); c4.setCellStyle(bmStyle);
                } else {
                    for (int i = 0; i < maxCount; i++) {
                        Row addrRow = sheet.createRow(rowIdx++);
                        Row timeRow = sheet.createRow(rowIdx++);

                        if (i == 0) {
                            addrRow.createCell(0).setCellValue(nr);
                            addrRow.createCell(1).setCellValue(imie);
                            addrRow.createCell(2).setCellValue(nazwisko);
                            addrRow.createCell(3).setCellValue(plh);
                            addrRow.createCell(4).setCellValue(totalHours);
                        }

                        for (int d = 0; d < dates.size(); d++) {
                            String dateKey = dates.get(d);
                            List<Map<String, Object>> list = akcjeByDate.getOrDefault(dateKey, Collections.emptyList());
                            if (i < list.size()) {
                                Map<String, Object> action = list.get(i);
                                String address = Optional.ofNullable(action.get("address")).map(Object::toString).orElse("");
                                String start = Optional.ofNullable(action.get("start_sys")).map(Object::toString).orElse("");
                                String stop = Optional.ofNullable(action.get("stop_sys")).map(Object::toString).orElse("");

                                Cell addrCell = addrRow.createCell(5 + d);
                                addrCell.setCellValue(address);
                                addrCell.setCellStyle(addressStyle);

                                Cell timeCell = timeRow.createCell(5 + d);
                                timeCell.setCellValue((start.isEmpty() && stop.isEmpty()) ? "" : (start + " - " + stop));
                                timeCell.setCellStyle(timeStyle);
                            }
                        }
                    }
                }
                nr++;
            }

            wb.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private String formatTime(LocalDateTime dateTime) {
        return dateTime.toLocalTime().format(TIME_FMT);
    }

    @GetMapping(value = "/dyspo", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<?> getDyspo(
            @CookieValue(value = "Authtoken", required = true) String authToken,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "from", required = false) String fromDateStr,
            @RequestParam(value = "to", required = false) String toDateStr
    ) {
        try {
            // Security check
            Security securityResponse = Security.check_security_SV(authToken, userRepository, supervisorRepository);
            if (!securityResponse.success) {
                Map<String, Object> body = Map.of("message", securityResponse.message);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
            }
            Supervisor supervisor = (Supervisor) securityResponse.data;

            Optional<Team> optionalTeam = teamRepository.findBySupervisor(supervisor);
            if (optionalTeam.isEmpty()) {
                Map<String, Object> body = Map.of("message", "Team not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
            }
            Team team = optionalTeam.get();

            // Determine date columns: either explicit range (from/to) or the month (year/month)
            List<String> dates;
            LocalDate rangeStart = null;
            LocalDate rangeEnd = null;
            boolean usingRange = false;

            if ((fromDateStr != null && !fromDateStr.isBlank()) || (toDateStr != null && !toDateStr.isBlank())) {
                // require both
                if (fromDateStr == null || fromDateStr.isBlank() || toDateStr == null || toDateStr.isBlank()) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Both 'from' and 'to' must be provided when using date range."));
                }
                try {
                    rangeStart = LocalDate.parse(fromDateStr, DAY_FMT);
                    rangeEnd = LocalDate.parse(toDateStr, DAY_FMT);
                } catch (DateTimeParseException ex) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Date parsing failed. Use dd.MM.yyyy format.", "error", ex.getMessage()));
                }
                if (rangeEnd.isBefore(rangeStart)) {
                    return ResponseEntity.badRequest().body(Map.of("message", "'to' must be same or after 'from'."));
                }
                usingRange = true;

                // build list of date strings between start..end inclusive
                long days = ChronoUnit.DAYS.between(rangeStart, rangeEnd);
                LocalDate finalRangeStart = rangeStart;
                dates = IntStream.rangeClosed(0, (int) days)
                        .mapToObj(i -> finalRangeStart.plusDays(i).format(DAY_FMT))
                        .collect(Collectors.toList());
            } else {
                // Build YearMonth from params or default to now
                YearMonth ym;
                if (year != null && month != null) {
                    ym = YearMonth.of(year, Math.max(1, Math.min(12, month)));
                } else if (month != null) {
                    int y = Year.now().getValue();
                    ym = YearMonth.of(y, Math.max(1, Math.min(12, month)));
                } else {
                    ym = YearMonth.now();
                }
                int yearVal = ym.getYear();
                Month mVal = ym.getMonth();
                dates = IntStream.rangeClosed(1, ym.lengthOfMonth())
                        .mapToObj(d -> LocalDate.of(yearVal, mVal, d).format(DAY_FMT))
                        .collect(Collectors.toList());
                rangeStart = LocalDate.of(yearVal, mVal, 1);
                rangeEnd = LocalDate.of(yearVal, mVal, ym.lengthOfMonth());
            }

            // Collect brandmasters for team
            List<Brandmaster> brandmasters = brandmasterRepository.findByTeam(team);

            // Build BM data list, only including akcje that fall within rangeStart..rangeEnd
            List<Map<String, Object>> bmDataList = new ArrayList<>();
            for (Brandmaster bm : brandmasters) {
                Map<String, Object> bmData = new HashMap<>();
                bmData.put("id_bm", bm.getIdBm());
                User user = bm.getUser();
                bmData.put("imie", user != null ? user.getImie() : "");
                bmData.put("nazwisko", user != null ? user.getNazwisko() : "");
                bmData.put("login", user != null ? user.getLogin() : "");
                bmData.put("sv_id", supervisor.getIdSv());
                bmData.put("team_id", team.getIdTeam());
                bmData.put("type", team.getType());
                bmData.put("area_name", team.getArea() != null ? team.getArea().getArea_name() : "");

                // Fetch akcje and filter by date range
                List<Akcja> akcje = akcjaRepository.findByBrandmasterOrderByDataAsc(bm);
                List<Map<String, Object>> akcjeData = new ArrayList<>();
                for (Akcja akcja : akcje) {
                    if (akcja.getData() == null) continue;
                    if (akcja.getData().isBefore(rangeStart) || akcja.getData().isAfter(rangeEnd)) continue;

                    Map<String, Object> map = new HashMap<>();
                    map.put("id_akcja", akcja.getIdAkcja());

                    boolean done = akcja.getRealStart() != null || akcja.getRealStop() != null;
                    map.put("status", done ? "Odbyta" : "Nadchodzaca");

                    map.put("date", akcja.getData().format(DAY_FMT));

                    Shop shop = akcja.getShop() != null ? shopRepository.findById(akcja.getShop().getIdShop()).orElse(null) : null;
                    map.put("type", shop != null ? shop.getName() : "Brak");
                    map.put("address", shop != null ? shop.getAddress() : "Brak");
                    map.put("szkolenie", akcja.isSzkolenie());

                    map.put("start_sys", akcja.getPlannedStart() != null ? formatTime(akcja.getPlannedStart()) : "");
                    map.put("stop_sys", akcja.getPlannedStop() != null ? formatTime(akcja.getPlannedStop()) : "");

                    map.put("start_real", akcja.getRealStart() != null ? formatTime(akcja.getRealStart()) : "");
                    map.put("stop_real", akcja.getRealStop() != null ? formatTime(akcja.getRealStop()) : "");

                    akcjeData.add(map);
                }

                bmData.put("akcje", akcjeData);
                bmDataList.add(bmData);
            }

            // Export
            ByteArrayInputStream in = exportBmDataToExcel(bmDataList, dates);
            byte[] bytes = in.readAllBytes();

            String filename;
            if (usingRange) {
                filename = String.format("dyspozycja_%s-%s.xlsx", rangeStart.format(DateTimeFormatter.ofPattern("yyyyMMdd")), rangeEnd.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            } else {
                filename = String.format("dyspozycja_%d_%02d.xlsx", rangeStart.getYear(), rangeStart.getMonthValue());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.builder("attachment").filename(filename).build());
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

            return ResponseEntity.ok().headers(headers).body(bytes);

        } catch (IOException e) {
            log.error("Failed to generate Excel file", e);
            Map<String, Object> body = Map.of("message", "Failed to generate Excel file", "error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        } catch (Exception e) {
            log.error("Unexpected server error", e);
            Map<String, Object> body = Map.of("message", "Unexpected server error", "error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }
}
