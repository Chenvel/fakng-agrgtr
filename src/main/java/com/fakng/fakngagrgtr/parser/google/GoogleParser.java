package com.fakng.fakngagrgtr.parser.google;

import com.fakng.fakngagrgtr.company.CompanyRepository;
import com.fakng.fakngagrgtr.parser.ApiParser;
import com.fakng.fakngagrgtr.parser.LocationProcessor;
import com.fakng.fakngagrgtr.vacancy.Vacancy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class GoogleParser extends ApiParser {

    public GoogleParser(WebClient webClient, CompanyRepository companyRepository, LocationProcessor locationProcessor,
                        @Value("${url.google}") String url) {
        super(webClient, companyRepository, locationProcessor);
        this.url = url;
    }

    @PostConstruct
    public void init() {
        initBase();
    }

    @Override
    protected String getCompanyName() {
        return "Google";
    }

    @Override
    protected List<Vacancy> getAllVacancies() {
        ResponseDTO firstPage = getPage(1);
        int lastPage = firstPage.getCount() / firstPage.getPageSize() + 1;
        List<Vacancy> allVacancies = new ArrayList<>(processPageResponse(firstPage));
        for (int index = 2; index <= lastPage; index++) {
            allVacancies.addAll(processPageResponse(getPage(index)));
        }
        return allVacancies;
    }

    private List<Vacancy> processPageResponse(ResponseDTO response) {
        return response.getJobs().stream()
                .map(this::createVacancy)
                .toList();
    }

    private Vacancy createVacancy(VacancyDTO dto) {
        Vacancy vacancy = new Vacancy();
        vacancy.setId(parseVacancyId(dto.getId()));
        vacancy.setTitle(dto.getTitle());
        vacancy.setUrl(dto.getApplyUrl());
        vacancy.setCompany(company);
        processLocations(vacancy, dto.getLocations());
        vacancy.setDescription(generateFullDescription(dto));
        return vacancy;
    }

    private void processLocations(Vacancy vacancy, List<LocationDTO> locations) {
        locations.forEach(location -> vacancy.addLocation(
                locationProcessor.processLocation(company, location.getCity(), location.getCountryCode()))
        );
    }

    private Long parseVacancyId(String dtoId) {
        return Long.parseLong(dtoId.split("/")[1]);
    }

    private String generateFullDescription(VacancyDTO dto) {
        return dto.getDescription() + "\n" +
                dto.getSummary() + "\n" +
                dto.getQualifications() + "\n" +
                dto.getResponsibilities() + "\n" +
                dto.getAdditionalInstructions() + "\n" +
                "Has remote: " + dto.getHasRemote();
    }

    private ResponseDTO getPage(int page) {
        ResponseSpec response = sendRequest(String.format(url, page));
        return response.bodyToMono(ResponseDTO.class).block();
    }

    private ResponseSpec sendRequest(String url) {
        return webClient
                .get()
                .uri(url)
                .retrieve();
    }
}
