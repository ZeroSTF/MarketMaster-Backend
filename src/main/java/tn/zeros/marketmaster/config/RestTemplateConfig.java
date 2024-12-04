package tn.zeros.marketmaster.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());  // Register module for Java 8 date/time support

        converter.setObjectMapper(mapper);
        restTemplate.getMessageConverters().add(0, converter);

        return restTemplate;
    }
}
