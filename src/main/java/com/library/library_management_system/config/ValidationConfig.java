package com.library.library_management_system.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import jakarta.validation.Validator;
import java.nio.charset.StandardCharsets;

/**
 * Validation configuration for Bean Validation and custom error messages
 */
@Configuration
@Slf4j
public class ValidationConfig {

    /**
     * Message source for validation error messages
     */
    @Bean
    public MessageSource validationMessageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages/validation");
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setCacheSeconds(300); // Cache for 5 minutes
        messageSource.setFallbackToSystemLocale(false);

        log.info("Configured validation message source");
        return messageSource;
    }

    /**
     * Custom validator factory with custom message source
     */
    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.setValidationMessageSource(validationMessageSource());

        log.info("Configured custom validator factory");
        return factory;
    }

    /**
     * Method validation post processor for @Validated annotation
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(validator());

        log.info("Configured method validation post processor");
        return processor;
    }

    /**
     * Primary validator bean
     */
    @Bean
    public Validator validatorBean() {
        return validator();
    }
}