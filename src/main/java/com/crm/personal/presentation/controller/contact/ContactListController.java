package com.crm.personal.presentation.controller.contact;

import com.crm.personal.application.contact.command.SearchContactsQuery;
import com.crm.personal.application.contact.dto.ContactDto;
import com.crm.personal.application.contact.port.SearchContactsUseCase;
import com.crm.personal.presentation.javafx.event.ContactSelectedEvent;
import com.crm.personal.presentation.javafx.event.TagFilterChangedEvent;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContactListController {

    private final SearchContactsUseCase searchContactsUseCase;
    private final ApplicationEventPublisher eventPublisher;

    @FXML
    private ListView<ContactDto> contactList;

    public ContactListController(
            SearchContactsUseCase searchContactsUseCase,
            ApplicationEventPublisher eventPublisher
    ) {
        this.searchContactsUseCase = searchContactsUseCase;
        this.eventPublisher = eventPublisher;
    }

    @FXML
    private void initialize() {
        contactList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && newValue.id() != null) {
                eventPublisher.publishEvent(new ContactSelectedEvent(newValue.id()));
            }
        });
    }

    @EventListener
    public void onTagFilterChanged(TagFilterChangedEvent event) {
        List<ContactDto> contacts = searchContactsUseCase.search(new SearchContactsQuery(
                "",
                event.selectedTagIds(),
                event.operator().name()
        ));

        Platform.runLater(() -> contactList.setItems(FXCollections.observableArrayList(contacts)));
    }
}
