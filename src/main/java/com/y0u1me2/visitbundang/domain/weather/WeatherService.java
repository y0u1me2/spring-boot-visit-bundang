package com.y0u1me2.visitbundang.domain.weather;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeatherService {
    @Value("${open-api-auth-key}")
    private String serviceKey;

    private static String base_date; // 년+월+일 ex) 20230524
    private static String base_time; // 시간 ex) 1851

    public Map<String, Object> getPresentWeather() {
        /*
            현재 날씨 데이터를 조회하여 가져온다.
            기온, 습도, 풍속, 강수여부, 강수량 의 정보를 수집하여 반환한다.
         */
        Map<String, Object> map = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();

        String response;
        JsonNode jsonNode;
        ObjectMapper objectMapper = new ObjectMapper();


        WebClient webClient = WebClient.builder()
                .baseUrl("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst")
                .build();

        while (true) {
            base_date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            base_time = String.format("%02d", hour) + String.format("%02d", now.getMinute());

            response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("pageNo", 1)
                            .queryParam("numOfRows", 10)
                            .queryParam("base_date", base_date)
                            .queryParam("base_time", base_time)
                            .queryParam("nx", 62)
                            .queryParam("ny", 123)
                            .queryParam("dataType", "JSON")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class).block();

            try {
                jsonNode = objectMapper.readTree(response);
                String resultCode = jsonNode.at("/response/header/resultCode").asText();
                if (resultCode.equals("00")) {
                    break;
                } else {
                    if (hour > 0) {
                        hour--;
                    } else {
                        now = now.minusDays(1);
                        hour = 23;
                    }
                }
            } catch (JsonMappingException e) {
                throw new RuntimeException(e);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        JsonNode items = jsonNode.at("/response/body/items/item");
        if (items.isArray()) {
            loop: for (JsonNode item : items) {
                String category = item.get("category").asText();

                String date = item.get("baseDate").asText();
                String time = item.get("baseTime").asText();
                StringBuilder sb = new StringBuilder();
                sb.append(date.substring(0, 4)).append("-").append(date.substring(4, 6)).append("-").append(date.substring(6));
                sb.append(" ").append(time.substring(0, 2)).append(":").append(time.substring(2));

                switch (category) {
                    case "T1H":
                        map.put("temperature", item.get("obsrValue").asDouble());
                        map.put("temperature_base_time", sb.toString());
                        break;
                    case "PTY":
                        map.put("rainfall", item.get("obsrValue").asDouble());
                        map.put("rainfall_base_time", sb.toString());
                        break;
                    case "REH":
                        map.put("humidity", item.get("obsrValue").asDouble());
                        map.put("humidity_base_time", sb.toString());
                        break;
                    case "WSD":
                        map.put("wind", item.get("obsrValue").asDouble());
                        map.put("wind_base_time", sb.toString());
                        break;
                    case "RN1":
                        map.put("precipitation", item.get("obsrValue").asDouble());
                        map.put("precipitation_base_time", sb.toString());
                        break;
                    default:
                        continue loop;
                }
            }
        }

        return map;
    }

    public Map<String, Object> getPresentDust() {
        /*
            실시간 미세먼지 정보를 조회한다.
         */
        Map<String, Object> present_dust = new HashMap<>();

        WebClient webClient = WebClient.builder()
                .baseUrl("http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty")
                .build();

        String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("stationName", "수내동")
                        .queryParam("dataTerm", "DAILY")
                        .queryParam("returnType", "JSON")
                        .queryParam("ver", "1.0")
                        .build())
                .retrieve()
                .bodyToMono(String.class).block();

        
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            String resultCode = jsonNode.at("/response/header/resultCode").asText();
            if (!resultCode.equals("00")) {
                // TODO: 오류코드가 발생할 경우 조치 필요
                return present_dust;
            } else {
                JsonNode items = jsonNode.at("/response/body/items");
                if (items.isArray()) {
                    for (JsonNode item : items) {
                        String pm10StringValue = item.get("pm10Value").asText();
                        String pm25StringValue = item.get("pm10Value").asText();

                        if (!pm10StringValue.isBlank() && !pm25StringValue.isBlank()) {
                            // 미세먼지
                            int pm10IntValue = Integer.parseInt(pm10StringValue);
                            present_dust.put("pm10_value", pm10IntValue);
                            if (pm10IntValue > 150) {
                                present_dust.put("pm10_text", "매우 나쁨");
                            } else if (pm10IntValue > 80) {
                                present_dust.put("pm10_text", "나쁨");
                            } else if (pm10IntValue > 30) {
                                present_dust.put("pm10_text", "보통");
                            } else {
                                present_dust.put("pm10_text", "좋음");
                            }

                            // 초미세먼지
                            int pm25IntValue = Integer.parseInt(pm25StringValue);
                            present_dust.put("pm25_value", pm25IntValue);
                            if (pm25IntValue > 75) {
                                present_dust.put("pm25_text", "매우 나쁨");
                            } else if (pm25IntValue > 35) {
                                present_dust.put("pm25_text", "나쁨");
                            } else if (pm25IntValue > 15) {
                                present_dust.put("pm25_text", "보통");
                            } else {
                                present_dust.put("pm25_text", "좋음");
                            }

                            present_dust.put("base_time", item.get("dataTime").asText());

                            break; // 데이터를 한번에 정상적으로 조회한 경우 반복문을 빠져나간다.
                        } else {
                            continue;
                        }
                    }
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return present_dust;
    }

    public Map<String, Object> getCoronaInfo() {
        // 크롤링을 통해 성남시 코로나 데이터를 가져온다.
        Map<String, Object> coronaInfo = new HashMap<>();
        String url = "https://corona.seongnam.go.kr/";

        try {
            Document document = Jsoup.connect(url).get();

            Element today_stats = document.getElementsByClass("content_top").get(0);
            Element today_number = today_stats.getElementsByClass("accent").get(0);
            coronaInfo.put("today_number", today_number.text());
            Element base_time = today_stats.getElementsByTag("span").last();
            coronaInfo.put("base_time", base_time.text());

            Element total_stats = document.getElementsByClass("content_bottom").first();
            Element total_number = total_stats.getElementsByClass("accent").first();
            coronaInfo.put("total_number", total_number.text());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return coronaInfo;
    }
}
