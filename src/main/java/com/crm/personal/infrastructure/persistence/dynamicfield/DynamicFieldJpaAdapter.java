package com.crm.personal.infrastructure.persistence.dynamicfield;

import com.crm.personal.domain.dynamicfield.model.DynamicField;
import com.crm.personal.domain.dynamicfield.model.DynamicFieldId;
import com.crm.personal.domain.dynamicfield.model.DynamicFieldType;
import com.crm.personal.domain.dynamicfield.port.DynamicFieldRepositoryPort;
import com.crm.personal.infrastructure.persistence.repository.CampoDinamicoRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class DynamicFieldJpaAdapter implements DynamicFieldRepositoryPort {

    private final CampoDinamicoRepository repository;

    public DynamicFieldJpaAdapter(CampoDinamicoRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<DynamicField> findActive() {
        return repository.findByActivoTrue().stream()
                .map(field -> new DynamicField(
                        new DynamicFieldId(field.getId()),
                        field.getNombre(),
                        DynamicFieldType.valueOf(field.getTipo().toUpperCase(Locale.ROOT)),
                        field.isActivo()
                ))
                .toList();
    }

    @Override
    public Optional<DynamicField> findById(DynamicFieldId id) {
        return repository.findById(id.value())
                .map(field -> new DynamicField(
                        new DynamicFieldId(field.getId()),
                        field.getNombre(),
                        DynamicFieldType.valueOf(field.getTipo().toUpperCase(Locale.ROOT)),
                        field.isActivo()
                ));
    }
}
