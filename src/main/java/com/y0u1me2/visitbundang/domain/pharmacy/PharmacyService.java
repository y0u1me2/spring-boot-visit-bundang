package com.y0u1me2.visitbundang.domain.pharmacy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.y0u1me2.visitbundang.domain.pharmacy.dao.PharmacyRepository;
import com.y0u1me2.visitbundang.domain.pharmacy.dto.PharmacyDTO;
import com.y0u1me2.visitbundang.domain.pharmacy.entity.Pharmacy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PharmacyService {
    @Autowired
    private PharmacyRepository pharmacyRepository;

    @Value("${open-api-auth-key}")
    private String serviceKey;

    public List<PharmacyDTO> getPharmacies(int page) throws IOException {
        // 디비에 저장된 데이터가 없을 경우 API를 호출하여 데이터를 먼저 저장한다.
        if (pharmacyRepository.findAll().isEmpty()) {
            loadPharmacyData();
        }

        // 현재 시간을 기준으로 오픈 상태의 약국들만 추출하여 조회한다. (+ 페이징처리)
        Pageable pageable = PageRequest.of(page, 10);
        LocalDateTime now = LocalDateTime.now();
        int currentTime = now.getHour() * 60 + now.getMinute();
        Page<Pharmacy> pharmacies = pharmacyRepository.findByOpenTimeLessThanEqualAndCloseTimeGreaterThanEqual(currentTime, currentTime, pageable);

        // Entity를 DTO로 변환하여 결과값을 반환한다.
        List<PharmacyDTO> list = pharmacies.stream().map(e -> new PharmacyDTO(e)).collect(Collectors.toList());

        return list;
    }

    public void loadPharmacyData() {
        LocalDateTime now = LocalDateTime.now();
        int dayOfWeek = now.getDayOfWeek().getValue();

        WebClient webClient = WebClient.builder()
                .baseUrl("http://apis.data.go.kr/B552657/ErmctInsttInfoInqireService/getParmacyListInfoInqire")
                .build();

        String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("Q0", "경기도")
                        .queryParam("Q1", "분당구")
                        .queryParam("numOfRows", 300)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class).block();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(response);

            JsonNode header = rootNode.at("/response/header");
            String resultCode = header.get("resultCode").asText();
            if (!resultCode.equals("00")) {
                log.info("Loading data failed. API error msg : {}", header.get("resultMsg").asText());
                return;
            }

            JsonNode items = rootNode.at("/response/body/items/item");
            if (items.isArray()) {
                for (JsonNode item : items) {
                    // 디비에 데이터를 저장한다.
                    Pharmacy value = Pharmacy.builder()
                            .id(item.get("hpid").asText())
                            .name(item.get("dutyName").asText())
                            .address(item.get("dutyAddr").asText())
                            .phone(item.get("dutyTel1").asText())
                            .latitude(item.get("wgs84Lat").asDouble())
                            .longitude(item.get("wgs84Lon").asDouble())
                            .build();

                    // 오늘의 요일을 기준으로 약국의 오픈시간과 마감시간을 계산한다.
                    JsonNode openTimeNode = item.get("dutyTime" + dayOfWeek + "s");
                    JsonNode closeTimeNode = item.get("dutyTime" + dayOfWeek + "c");

                    if (openTimeNode != null && closeTimeNode != null) {
                        String openTimeString = openTimeNode.asText();
                        String closeTimeString = closeTimeNode.asText();

                        int openTime = Integer.parseInt(openTimeString.substring(0, 2)) * 60 + Integer.parseInt(openTimeString.substring(2));
                        int closeTime = Integer.parseInt(closeTimeString.substring(0, 2)) * 60 + Integer.parseInt(closeTimeString.substring(2));

                        value.setOpenTime(openTime);
                        value.setCloseTime(closeTime);
                    }
                    pharmacyRepository.save(value);
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void deletePharmacyData() {
        pharmacyRepository.deleteAllInBatch();
    }


}
