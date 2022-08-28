package com.fakng.fakngagrgtr.parser;

import com.fakng.fakngagrgtr.parser.stripe.StripeParser;
import com.fakng.fakngagrgtr.persistent.company.Company;
import com.fakng.fakngagrgtr.persistent.company.CompanyRepository;
import com.fakng.fakngagrgtr.persistent.vacancy.Vacancy;
import com.gargoylesoftware.htmlunit.WebClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class StripeParserTest extends AbstractParserTest {
    private static final String URL = "Stripe";
    private static final String STRIPE_VACANCY_URL = "https://stripe.com/title_1/111";
    private static final String DATA_PATH = "stripe-block-data.html";
    private static final String STRIPE_DATA_PATH = "stripe-data1.html";
    private StripeParser stripeParser;
    @Mock
    private WebClient htmlWebClient;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private LocationProcessor locationProcessor;

    @BeforeEach
    public void init() {
        stripeParser = new StripeParser(htmlWebClient, companyRepository, locationProcessor, URL);
    }

    @Test
    public void testParseCreatesVacanciesFromJsonWithCachedLocations() throws Exception {
        Company company = prepareCompany();
        Mockito.when(companyRepository.findByTitle(URL))
                .thenReturn(Optional.of(company));
        Mockito.when(locationProcessor.processLocation(company, "city_1", "country_1"))
                .thenReturn(company.getLocations().get(0));
        Mockito.when(locationProcessor.processLocation(company, "city_2", "country_2"))
                .thenReturn(company.getLocations().get(1));
        mockHtmlClient(trueClient -> Mockito.when(htmlWebClient.getPage(URL))
                .thenReturn(trueClient.loadHtmlCodeIntoCurrentWindow(getTestData(DATA_PATH))));
        mockHtmlClient(trueClient -> Mockito.when(htmlWebClient.getPage(STRIPE_VACANCY_URL))
                .thenReturn(trueClient.loadHtmlCodeIntoCurrentWindow(getTestData(STRIPE_DATA_PATH))));

        stripeParser.init();

        List<Vacancy> expectedVacancies = prepareVacancies(company);
        List<Vacancy> actualVacancies = stripeParser.parse();

        assertEquals(expectedVacancies.size(), actualVacancies.size());
        for (int i = 0; i < expectedVacancies.size(); i++) {
            assertVacancy(expectedVacancies.get(i), actualVacancies.get(i));
        }
    }

    private List<Vacancy> prepareVacancies(Company company) {
        List<Vacancy> vacancies = new ArrayList<>();

        Vacancy first = new Vacancy();
        first.setTitle("title_1");
        first.setJobId("111");
        first.setUrl(STRIPE_VACANCY_URL);
        first.setCompany(company);
        first.setDescription("description_1\nJob Type: time_1\nTeam: team_1");
        first.setLocations(company.getLocations());
        vacancies.add(first);

        return vacancies;
    }
}
