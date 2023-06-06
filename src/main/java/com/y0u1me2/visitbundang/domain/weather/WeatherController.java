package com.y0u1me2.visitbundang.domain.weather;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class WeatherController {
    @Autowired
    private WeatherService weatherService;

    @GetMapping("/hello")
    public Map<String, Object> index() {
        Map<String, Object> map = new HashMap<>();
        map.put("present_weather", weatherService.getPresentWeather());
        map.put("present_dust", weatherService.getPresentDust());
        map.put("corona_info", weatherService.getCoronaInfo());
        return map;
    }


}
