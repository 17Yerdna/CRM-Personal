package com.crm.personal.application.timeline.command;

import java.time.LocalDateTime;

public record AddTimelineNoteCommand(Long contactId, String title, String unsafeHtml, LocalDateTime date) {
}
