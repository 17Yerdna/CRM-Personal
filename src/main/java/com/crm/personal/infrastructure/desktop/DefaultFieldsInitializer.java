package com.crm.personal.infrastructure.desktop;

import com.crm.personal.infrastructure.persistence.model.CampoDinamico;
import com.crm.personal.infrastructure.persistence.repository.CampoDinamicoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DefaultFieldsInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultFieldsInitializer.class);

    private final CampoDinamicoRepository campoRepository;

    public DefaultFieldsInitializer(CampoDinamicoRepository campoRepository) {
        this.campoRepository = campoRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (campoRepository.count() == 0) {
            log.info("Creando campos dinámicos por defecto...");
            
            campoRepository.save(CampoDinamico.builder().nombre("Teléfono").tipo("TEXT").activo(true).build());
            campoRepository.save(CampoDinamico.builder().nombre("Email").tipo("TEXT").activo(true).build());
            campoRepository.save(CampoDinamico.builder().nombre("Empresa").tipo("TEXT").activo(true).build());
            campoRepository.save(CampoDinamico.builder().nombre("Fecha de nacimiento").tipo("DATE").activo(true).build());
            
            log.info("Campos dinámicos por defecto creados.");
        }
    }
}
