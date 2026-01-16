package ut.edu.vaccinationmanagementsystem.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.dto.VaccineDTO;
import ut.edu.vaccinationmanagementsystem.entity.Disease;
import ut.edu.vaccinationmanagementsystem.entity.Promotion;
import ut.edu.vaccinationmanagementsystem.entity.VaccinationCenter;
import ut.edu.vaccinationmanagementsystem.entity.Vaccine;
import ut.edu.vaccinationmanagementsystem.service.CenterVaccineService;
import ut.edu.vaccinationmanagementsystem.service.VaccineService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/vaccines")
public class VaccineController {
    
    @Autowired
    private VaccineService vaccineService;
    
    @Autowired
    private CenterVaccineService centerVaccineService;
    

    @GetMapping
    public ResponseEntity<?> getAllVaccines() {
        try {
            List<Vaccine> vaccines = vaccineService.getAllVaccines();
            Map<Long, Long> vaccinationCountMap = vaccineService.getVaccinationCountMap();
            LocalDateTime now = LocalDateTime.now();
            
            // Tạo response với thông tin bổ sung
            List<Map<String, Object>> vaccineResponses = vaccines.stream().map(vaccine -> {
                Map<String, Object> response = new HashMap<>();
                response.put("id", vaccine.getId());
                response.put("name", vaccine.getName());
                response.put("code", vaccine.getCode());
                response.put("manufacturer", vaccine.getManufacturer());
                response.put("origin", vaccine.getOrigin());
                response.put("description", vaccine.getDescription());
                response.put("price", vaccine.getPrice());
                response.put("minAge", vaccine.getMinAge());
                response.put("maxAge", vaccine.getMaxAge());
                response.put("dosesRequired", vaccine.getDosesRequired());
                response.put("daysBetweenDoses", vaccine.getDaysBetweenDoses());
                response.put("contraindications", vaccine.getContraindications());
                response.put("storageTemperature", vaccine.getStorageTemperature());
                response.put("imageUrl", vaccine.getImageUrl());
                response.put("status", vaccine.getStatus());
                response.put("createdAt", vaccine.getCreatedAt());
                
                // Thêm diseases
                if (vaccine.getDiseases() != null) {
                    List<Map<String, Object>> diseases = vaccine.getDiseases().stream().map(disease -> {
                        Map<String, Object> diseaseMap = new HashMap<>();
                        diseaseMap.put("id", disease.getId());
                        diseaseMap.put("code", disease.getCode());
                        diseaseMap.put("name", disease.getName());
                        return diseaseMap;
                    }).collect(Collectors.toList());
                    response.put("diseases", diseases);
                } else {
                    response.put("diseases", new ArrayList<>());
                }
                
                // Thêm promotions đang hoạt động
                List<Promotion> activePromotions = vaccineService.getActivePromotionsForVaccine(vaccine.getId());
                if (!activePromotions.isEmpty()) {
                    List<Map<String, Object>> promotions = activePromotions.stream().map(promotion -> {
                        Map<String, Object> promoMap = new HashMap<>();
                        promoMap.put("id", promotion.getId());
                        promoMap.put("name", promotion.getName());
                        promoMap.put("type", promotion.getType());
                        promoMap.put("discountPercentage", promotion.getDiscountPercentage());
                        promoMap.put("discountAmount", promotion.getDiscountAmount());
                        promoMap.put("startDate", promotion.getStartDate());
                        promoMap.put("endDate", promotion.getEndDate());
                        return promoMap;
                    }).collect(Collectors.toList());
                    response.put("promotions", promotions);
                    
                    // Tính giá sau ưu đãi (lấy promotion đầu tiên)
                    Promotion firstPromotion = activePromotions.get(0);
                    BigDecimal finalPrice = vaccine.getPrice();
                    if (firstPromotion.getType().toString().equals("PERCENTAGE") && firstPromotion.getDiscountPercentage() != null) {
                        BigDecimal discount = vaccine.getPrice().multiply(firstPromotion.getDiscountPercentage()).divide(new BigDecimal("100"));
                        finalPrice = vaccine.getPrice().subtract(discount);
                    } else if (firstPromotion.getType().toString().equals("FIXED_AMOUNT") && firstPromotion.getDiscountAmount() != null) {
                        finalPrice = vaccine.getPrice().subtract(firstPromotion.getDiscountAmount());
                        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
                            finalPrice = BigDecimal.ZERO;
                        }
                    }
                    response.put("finalPrice", finalPrice);
                } else {
                    response.put("promotions", new ArrayList<>());
                    response.put("finalPrice", vaccine.getPrice());
                }
                
                // Thêm số lượng đã tiêm (để tính "bán chạy nhất")
                Long vaccinationCount = vaccinationCountMap.getOrDefault(vaccine.getId(), 0L);
                response.put("vaccinationCount", vaccinationCount);
                
                return response;
            }).collect(Collectors.toList());
            
            System.out.println("Found " + vaccines.size() + " vaccines");
            return ResponseEntity.ok(vaccineResponses);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to load vaccines: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/vaccines/diseases
     * Lấy danh sách tất cả diseases
     */
    @GetMapping("/diseases")
    public ResponseEntity<?> getAllDiseases() {
        try {
            List<Disease> diseases = vaccineService.getAllDiseases();
            return ResponseEntity.ok(diseases);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to load diseases: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/vaccines/{id}
     * Xem chi tiết vaccine theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getVaccineById(@PathVariable Long id) {
        try {
            Vaccine vaccine = vaccineService.getVaccineById(id);
            Map<Long, Long> vaccinationCountMap = vaccineService.getVaccinationCountMap();
            LocalDateTime now = LocalDateTime.now();
            
            // Tạo response với thông tin đầy đủ
            Map<String, Object> response = new HashMap<>();
            response.put("id", vaccine.getId());
            response.put("name", vaccine.getName());
            response.put("code", vaccine.getCode());
            response.put("manufacturer", vaccine.getManufacturer());
            response.put("origin", vaccine.getOrigin());
            response.put("description", vaccine.getDescription());
            response.put("price", vaccine.getPrice());
            response.put("minAge", vaccine.getMinAge());
            response.put("maxAge", vaccine.getMaxAge());
            response.put("dosesRequired", vaccine.getDosesRequired());
            response.put("daysBetweenDoses", vaccine.getDaysBetweenDoses());
            response.put("contraindications", vaccine.getContraindications());
            response.put("storageTemperature", vaccine.getStorageTemperature());
            response.put("imageUrl", vaccine.getImageUrl());
            response.put("status", vaccine.getStatus());
            response.put("createdAt", vaccine.getCreatedAt());
            
            // Thêm diseases
            if (vaccine.getDiseases() != null) {
                List<Map<String, Object>> diseases = vaccine.getDiseases().stream().map(disease -> {
                    Map<String, Object> diseaseMap = new HashMap<>();
                    diseaseMap.put("id", disease.getId());
                    diseaseMap.put("code", disease.getCode());
                    diseaseMap.put("name", disease.getName());
                    return diseaseMap;
                }).collect(Collectors.toList());
                response.put("diseases", diseases);
            } else {
                response.put("diseases", new ArrayList<>());
            }
            
            // Thêm promotions đang hoạt động
            List<Promotion> activePromotions = vaccineService.getActivePromotionsForVaccine(vaccine.getId());
            if (!activePromotions.isEmpty()) {
                List<Map<String, Object>> promotions = activePromotions.stream().map(promotion -> {
                    Map<String, Object> promoMap = new HashMap<>();
                    promoMap.put("id", promotion.getId());
                    promoMap.put("name", promotion.getName());
                    promoMap.put("type", promotion.getType());
                    promoMap.put("discountPercentage", promotion.getDiscountPercentage());
                    promoMap.put("discountAmount", promotion.getDiscountAmount());
                    promoMap.put("startDate", promotion.getStartDate());
                    promoMap.put("endDate", promotion.getEndDate());
                    return promoMap;
                }).collect(Collectors.toList());
                response.put("promotions", promotions);
                
                // Tính giá sau ưu đãi
                Promotion firstPromotion = activePromotions.get(0);
                BigDecimal finalPrice = vaccine.getPrice();
                if (firstPromotion.getType().toString().equals("PERCENTAGE") && firstPromotion.getDiscountPercentage() != null) {
                    BigDecimal discount = vaccine.getPrice().multiply(firstPromotion.getDiscountPercentage()).divide(new BigDecimal("100"));
                    finalPrice = vaccine.getPrice().subtract(discount);
                } else if (firstPromotion.getType().toString().equals("FIXED_AMOUNT") && firstPromotion.getDiscountAmount() != null) {
                    finalPrice = vaccine.getPrice().subtract(firstPromotion.getDiscountAmount());
                    if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
                        finalPrice = BigDecimal.ZERO;
                    }
                }
                response.put("finalPrice", finalPrice);
            } else {
                response.put("promotions", new ArrayList<>());
                response.put("finalPrice", vaccine.getPrice());
            }
            
            // Thêm số lượng đã tiêm
            Long vaccinationCount = vaccinationCountMap.getOrDefault(vaccine.getId(), 0L);
            response.put("vaccinationCount", vaccinationCount);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/vaccines/search?keyword={keyword}
     * Tìm kiếm vaccine theo từ khóa
     */
    @GetMapping("/search")
    public ResponseEntity<List<Vaccine>> searchVaccines(@RequestParam(required = false) String keyword) {
        try {
            List<Vaccine> vaccines = vaccineService.searchVaccines(keyword);
            return ResponseEntity.ok(vaccines);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * POST /api/vaccines
     * Tạo vaccine mới
     */
    @PostMapping
    public ResponseEntity<?> createVaccine(@RequestBody VaccineDTO dto) {
        try {
            Vaccine createdVaccine = vaccineService.createVaccine(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdVaccine);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * PUT /api/vaccines/{id}
     * Cập nhật vaccine
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVaccine(@PathVariable Long id, @RequestBody VaccineDTO dto) {
        try {
            Vaccine updatedVaccine = vaccineService.updateVaccine(id, dto);
            return ResponseEntity.ok(updatedVaccine);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * DELETE /api/vaccines/{id}
     * Xóa vaccine
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVaccine(@PathVariable Long id) {
        try {
            vaccineService.deleteVaccine(id);
            Map<String, String> message = new HashMap<>();
            message.put("message", "Vaccine deleted successfully");
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/vaccines/{id}/centers
     * Lấy danh sách trung tâm có vaccine này
     */
    @GetMapping("/{id}/centers")
    public ResponseEntity<?> getCentersByVaccineId(@PathVariable Long id) {
        try {
            Vaccine vaccine = vaccineService.getVaccineById(id);
            
            // Lấy danh sách centers từ CenterVaccine (bảng center_vaccines có dữ liệu thực tế)
            List<Map<String, Object>> centers = centerVaccineService.getCentersByVaccineId(id).stream().map(cv -> {
                VaccinationCenter center = cv.getCenter();
                Map<String, Object> centerMap = new HashMap<>();
                centerMap.put("id", center.getId());
                centerMap.put("name", center.getName());
                centerMap.put("address", center.getAddress());
                centerMap.put("phoneNumber", center.getPhoneNumber());
                centerMap.put("email", center.getEmail());
                centerMap.put("status", center.getStatus());
                centerMap.put("stockQuantity", cv.getStockQuantity());
                centerMap.put("lastRestocked", cv.getLastRestocked());
                return centerMap;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(centers);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/vaccines/{id}/similar
     * Lấy danh sách vaccine tương tự (cùng disease)
     */
    @GetMapping("/{id}/similar")
    public ResponseEntity<?> getSimilarVaccines(@PathVariable Long id) {
        try {
            Vaccine vaccine = vaccineService.getVaccineById(id);
            List<Vaccine> allVaccines = vaccineService.getAllVaccines();
            
            // Lấy danh sách disease codes của vaccine hiện tại (final để dùng trong lambda)
            final Long currentVaccineId = vaccine.getId();
            final List<String> diseaseCodes;
            if (vaccine.getDiseases() != null) {
                diseaseCodes = vaccine.getDiseases().stream()
                    .map(d -> d.getCode())
                    .collect(Collectors.toList());
            } else {
                diseaseCodes = new ArrayList<>();
            }
            
            // Tìm vaccine tương tự (có cùng ít nhất 1 disease, nhưng khác vaccine hiện tại)
            List<Map<String, Object>> similarVaccines = allVaccines.stream()
                .filter(v -> !v.getId().equals(currentVaccineId) && v.getStatus().toString().equals("AVAILABLE"))
                .filter(v -> {
                    if (v.getDiseases() == null) return false;
                    return v.getDiseases().stream()
                        .anyMatch(d -> diseaseCodes.contains(d.getCode()));
                })
                .limit(4) // Giới hạn 4 vaccine tương tự
                .map(v -> {
                    Map<String, Object> vMap = new HashMap<>();
                    vMap.put("id", v.getId());
                    vMap.put("name", v.getName());
                    vMap.put("code", v.getCode());
                    vMap.put("price", v.getPrice());
                    vMap.put("origin", v.getOrigin());
                    vMap.put("imageUrl", v.getImageUrl());
                    vMap.put("description", v.getDescription());
                    if (v.getDiseases() != null && !v.getDiseases().isEmpty()) {
                        vMap.put("diseaseName", v.getDiseases().get(0).getName());
                    }
                    return vMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(similarVaccines);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

