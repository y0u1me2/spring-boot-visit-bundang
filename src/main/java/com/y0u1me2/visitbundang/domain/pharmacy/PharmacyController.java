package com.y0u1me2.visitbundang.domain.pharmacy;

import com.y0u1me2.visitbundang.domain.pharmacy.dto.PharmacyDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/pharmacy")
public class PharmacyController {
    @Autowired
    private PharmacyService pharmacyService;

    @GetMapping("/list")
    public List<PharmacyDTO> list(@RequestParam(value = "page", defaultValue = "0") int page) throws IOException {
        List<PharmacyDTO> list = pharmacyService.getPharmacies(page);
        return list;
    }
}
