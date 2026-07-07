package com.crm.personal.infrastructure.config;

import com.crm.personal.application.contact.service.ContactApplicationService;
import com.crm.personal.application.dynamicfield.service.DynamicFieldApplicationService;
import com.crm.personal.application.shared.HtmlSanitizerPort;
import com.crm.personal.application.timeline.service.TimelineApplicationService;
import com.crm.personal.domain.contact.port.ContactRepositoryPort;
import com.crm.personal.domain.dynamicfield.port.DynamicFieldRepositoryPort;
import com.crm.personal.domain.dynamicfield.port.DynamicFieldValueRepositoryPort;
import com.crm.personal.domain.timeline.port.TimelineRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationUseCaseConfig {

    @Bean
    ContactApplicationService contactApplicationService(ContactRepositoryPort contacts) {
        return new ContactApplicationService(contacts);
    }

    @Bean
    DynamicFieldApplicationService dynamicFieldApplicationService(
            DynamicFieldRepositoryPort fields,
            DynamicFieldValueRepositoryPort values
    ) {
        return new DynamicFieldApplicationService(fields, values);
    }

    @Bean
    TimelineApplicationService timelineApplicationService(
            TimelineRepositoryPort timelineRepository,
            HtmlSanitizerPort htmlSanitizer
    ) {
        return new TimelineApplicationService(timelineRepository, htmlSanitizer);
    }
}
