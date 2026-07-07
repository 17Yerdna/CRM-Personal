package com.crm.personal.application.desktop.port;

import com.crm.personal.application.desktop.command.DesktopSaveCampoDinamicoCommand;
import com.crm.personal.application.desktop.command.DesktopSaveContactoCommand;
import com.crm.personal.application.desktop.command.DesktopSaveEtiquetaCommand;
import com.crm.personal.application.desktop.command.DesktopSaveMediaCommand;
import com.crm.personal.application.desktop.dto.DesktopCampoDinamicoDto;
import com.crm.personal.application.desktop.dto.DesktopContactoDto;
import com.crm.personal.application.desktop.dto.DesktopEtiquetaDto;
import com.crm.personal.application.desktop.dto.DesktopTimelineRecordDto;
import com.crm.personal.application.dto.ImportResultDTO;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface DesktopCrmUseCase {

    List<DesktopContactoDto> findAllContacts();
    DesktopContactoDto loadContact(Long id);
    List<DesktopContactoDto> searchContacts(String text, List<Long> etiquetaIds, String operator);
    void saveContact(DesktopSaveContactoCommand command);
    void deleteContact(Long id);

    List<DesktopEtiquetaDto> findAllTags();
    void saveTag(DesktopSaveEtiquetaCommand command);
    void deleteTag(Long id);

    List<DesktopCampoDinamicoDto> findActiveFields();
    List<DesktopCampoDinamicoDto> findAllFields();
    void saveCampoDinamico(DesktopSaveCampoDinamicoCommand command);
    void deleteCampoDinamico(Long id);

    List<DesktopTimelineRecordDto> getTimeline(Long contactId);
    void addNote(Long contactId, String title, String html);
    void addMedia(Long contactId, String title, List<DesktopSaveMediaCommand> media);
    void updateNote(Long recordId, String title, String html);
    void deleteTimelineRecord(Long recordId);

    byte[] exportContactToPdf(Long contactId) throws IOException;
    byte[] generateExcelTemplate() throws IOException;
    ImportResultDTO importFromExcel(InputStream inputStream);
}
