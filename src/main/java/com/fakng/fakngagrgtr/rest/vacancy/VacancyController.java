package com.fakng.fakngagrgtr.rest.vacancy;

import com.fakng.fakngagrgtr.service.VacancyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class VacancyController {

    private final VacancyService vacancyService;
    private final ObjectMapper objectMapper;

    @GetMapping("/vacancy")
    public Page<VacancyDTO> getVacancies(@RequestParam List<Long> companyIds, @RequestParam List<Long> locationIds,
                                         @RequestParam int page, @RequestParam int pageSize) {
        return vacancyService.findAll(companyIds, locationIds, page, pageSize)
                .map(vacancy -> objectMapper.convertValue(vacancy, VacancyDTO.class));
    }
}
