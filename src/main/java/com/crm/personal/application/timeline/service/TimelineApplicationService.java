package com.crm.personal.application.timeline.service;

import com.crm.personal.application.shared.HtmlSanitizerPort;
import com.crm.personal.application.timeline.command.AddTimelineNoteCommand;
import com.crm.personal.application.timeline.port.AddTimelineNoteUseCase;
import com.crm.personal.domain.contact.model.ContactId;
import com.crm.personal.domain.timeline.model.TimelineRecord;
import com.crm.personal.domain.timeline.port.TimelineRepositoryPort;

public class TimelineApplicationService implements AddTimelineNoteUseCase {

    private final TimelineRepositoryPort timelineRepository;
    private final HtmlSanitizerPort htmlSanitizer;

    public TimelineApplicationService(
            TimelineRepositoryPort timelineRepository,
            HtmlSanitizerPort htmlSanitizer
    ) {
        this.timelineRepository = timelineRepository;
        this.htmlSanitizer = htmlSanitizer;
    }

    @Override
    public void addNote(AddTimelineNoteCommand command) {
        String safeHtml = htmlSanitizer.sanitize(command.unsafeHtml());

        TimelineRecord note = TimelineRecord.note(
                new ContactId(command.contactId()),
                command.title(),
                safeHtml,
                command.date()
        );

        timelineRepository.save(note);
    }
}
