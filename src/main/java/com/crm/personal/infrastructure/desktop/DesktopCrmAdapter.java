package com.crm.personal.infrastructure.desktop;

import com.crm.personal.application.contact.command.CreateContactCommand;
import com.crm.personal.application.contact.command.UpdateContactCommand;
import com.crm.personal.application.contact.port.CreateContactUseCase;
import com.crm.personal.application.contact.port.UpdateContactUseCase;
import com.crm.personal.application.desktop.command.DesktopSaveContactoCommand;
import com.crm.personal.application.desktop.command.DesktopSaveEtiquetaCommand;
import com.crm.personal.application.desktop.command.DesktopSaveMediaCommand;
import com.crm.personal.application.desktop.dto.*;
import com.crm.personal.application.desktop.port.DesktopCrmUseCase;
import com.crm.personal.application.dto.EtiquetaDTO;
import com.crm.personal.application.dto.ImportResultDTO;
import com.crm.personal.infrastructure.persistence.model.*;
import com.crm.personal.infrastructure.persistence.repository.CampoDinamicoRepository;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DesktopCrmAdapter implements DesktopCrmUseCase {

    private final ContactoService contactoService;
    private final EtiquetaService etiquetaService;
    private final TimelineService timelineService;
    private final ExportImportService exportImportService;
    private final CampoDinamicoRepository campoDinamicoRepository;
    private final CreateContactUseCase createContactUseCase;
    private final UpdateContactUseCase updateContactUseCase;

    public DesktopCrmAdapter(
            ContactoService contactoService,
            EtiquetaService etiquetaService,
            TimelineService timelineService,
            ExportImportService exportImportService,
            CampoDinamicoRepository campoDinamicoRepository,
            CreateContactUseCase createContactUseCase,
            UpdateContactUseCase updateContactUseCase
    ) {
        this.contactoService = contactoService;
        this.etiquetaService = etiquetaService;
        this.timelineService = timelineService;
        this.exportImportService = exportImportService;
        this.campoDinamicoRepository = campoDinamicoRepository;
        this.createContactUseCase = createContactUseCase;
        this.updateContactUseCase = updateContactUseCase;
    }

    @Override
    public List<DesktopContactoDto> findAllContacts() {
        return contactoService.findAll().stream().map(this::toContactDto).toList();
    }

    @Override
    public DesktopContactoDto loadContact(Long id) {
        return toContactDto(contactoService.loadFull(id));
    }

    @Override
    public List<DesktopContactoDto> searchContacts(String text, List<Long> etiquetaIds, String operator) {
        var criteria = com.crm.personal.application.dto.SearchCriteriaDTO.builder()
                .texto(text)
                .etiquetaIds(etiquetaIds == null || etiquetaIds.isEmpty() ? null : etiquetaIds)
                .operador("OR".equalsIgnoreCase(operator)
                        ? com.crm.personal.application.dto.SearchOperator.OR
                        : com.crm.personal.application.dto.SearchOperator.AND)
                .build();
        return contactoService.search(criteria).stream().map(this::toContactDto).toList();
    }

    @Override
    public void saveContact(DesktopSaveContactoCommand command) {
        if (command.id() == null) {
            createContactUseCase.create(new CreateContactCommand(
                    command.nombre(), command.dni(), command.direccion(), command.fotoPerfilPath(),
                    command.etiquetaIds(), command.camposDinamicos()
            ));
            return;
        }

        updateContactUseCase.update(new UpdateContactCommand(
                command.id(), command.nombre(), command.dni(), command.direccion(), command.fotoPerfilPath(),
                command.etiquetaIds(), command.camposDinamicos()
        ));
    }

    @Override
    public void deleteContact(Long id) {
        contactoService.delete(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DesktopEtiquetaDto> findAllTags() {
        return etiquetaService.findAll().stream()
                .map(this::toTagDto)
                .toList();
    }

    @Override
    public void saveTag(DesktopSaveEtiquetaCommand command) {
        etiquetaService.save(EtiquetaDTO.builder()
                .id(command.id())
                .nombre(command.nombre())
                .colorHex(command.colorHex())
                .build());
    }

    @Override
    public void deleteTag(Long id) {
        etiquetaService.delete(id);
    }

    @Override
    public List<DesktopCampoDinamicoDto> findActiveFields() {
        return campoDinamicoRepository.findByActivoTrue().stream()
                .map(field -> new DesktopCampoDinamicoDto(field.getId(), field.getNombre(), field.getTipo()))
                .toList();
    }

    @Override
    public List<DesktopTimelineRecordDto> getTimeline(Long contactId) {
        return timelineService.getTimeline(contactId).stream().map(this::toTimelineDto).toList();
    }

    @Override
    public void addNote(Long contactId, String title, String html) {
        timelineService.addNota(contactId, title, html);
    }

    @Override
    public void addMedia(Long contactId, String title, List<DesktopSaveMediaCommand> media) {
        timelineService.addMedia(contactId, title, media.stream().map(this::toMediaEntity).toList());
    }

    @Override
    public void updateNote(Long recordId, String title, String html) {
        timelineService.updateNota(recordId, title, html);
    }

    @Override
    public void deleteTimelineRecord(Long recordId) {
        timelineService.deleteRecord(recordId);
    }

    @Override
    public byte[] exportContactToPdf(Long contactId) throws IOException {
        return exportImportService.exportContactoToPdf(contactId);
    }

    @Override
    public byte[] generateExcelTemplate() throws IOException {
        return exportImportService.generarPlantillaExcel();
    }

    @Override
    public ImportResultDTO importFromExcel(InputStream inputStream) {
        return exportImportService.importarDesdeExcel(inputStream);
    }

    private DesktopContactoDto toContactDto(Contacto contacto) {
        Set<DesktopEtiquetaDto> tags = new HashSet<>();
        if (contacto.getEtiquetas() != null) {
            contacto.getEtiquetas().forEach(tag -> tags.add(toTagDto(tag)));
        }

        List<DesktopCampoValorDto> fields = contacto.getCamposDinamicos() == null ? List.of() : contacto.getCamposDinamicos().stream()
                .map(value -> new DesktopCampoValorDto(value.getCampo().getId(), value.getCampo().getNombre(), value.getValor()))
                .toList();

        return new DesktopContactoDto(
                contacto.getId(), contacto.getNombre(), contacto.getDni(), contacto.getDireccion(),
                contacto.getFotoPerfilPath(), tags, fields
        );
    }

    private DesktopEtiquetaDto toTagDto(Etiqueta etiqueta) {
        return new DesktopEtiquetaDto(
                etiqueta.getId(),
                etiqueta.getNombre(),
                etiqueta.getColorHex()
        );
    }

    private DesktopTimelineRecordDto toTimelineDto(TimelineRecord record) {
        return new DesktopTimelineRecordDto(
                record.getId(), record.getTipo().name(), record.getTitulo(), record.getContenidoHtml(), record.getFecha(),
                record.getAdjuntos() == null ? List.of() : record.getAdjuntos().stream().map(this::toMediaDto).toList()
        );
    }

    private DesktopMediaAdjuntoDto toMediaDto(MediaAdjunto media) {
        return new DesktopMediaAdjuntoDto(
                media.getId(), media.getFilePath(), media.getNombreArchivo(), media.getDescripcion(),
                media.getLugar(), media.getFechaCaptura(), media.getTipoMime()
        );
    }

    private MediaAdjunto toMediaEntity(DesktopSaveMediaCommand media) {
        return MediaAdjunto.builder()
                .filePath(media.filePath())
                .nombreArchivo(media.nombreArchivo())
                .descripcion(media.descripcion())
                .lugar(media.lugar())
                .fechaCaptura(media.fechaCaptura())
                .tipoMime(media.tipoMime())
                .build();
    }
}
