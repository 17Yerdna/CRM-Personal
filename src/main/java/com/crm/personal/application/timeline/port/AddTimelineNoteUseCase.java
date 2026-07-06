package com.crm.personal.application.timeline.port;

import com.crm.personal.application.timeline.command.AddTimelineNoteCommand;

public interface AddTimelineNoteUseCase {

    void addNote(AddTimelineNoteCommand command);
}
