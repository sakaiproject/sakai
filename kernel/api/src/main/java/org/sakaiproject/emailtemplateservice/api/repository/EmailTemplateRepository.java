package org.sakaiproject.emailtemplateservice.api.repository;

import java.util.Optional;

import org.sakaiproject.emailtemplateservice.api.model.EmailTemplate;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface EmailTemplateRepository extends SpringCrudRepository<EmailTemplate, Long> {

    Optional<EmailTemplate> findByKeyAndLocale(String key, String locale);
}
