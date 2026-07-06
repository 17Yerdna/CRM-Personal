package com.crm.personal.presentation.controller.tag;

import com.crm.personal.presentation.javafx.event.TagFilterChangedEvent;
import com.crm.personal.presentation.javafx.event.TagFilterOperator;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class SidebarTagsController {

    private final ApplicationEventPublisher eventPublisher;

    @FXML
    private TreeView<TagNodeViewModel> tagTree;

    @FXML
    private RadioButton andRadio;

    public SidebarTagsController(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @FXML
    private void initialize() {
        tagTree.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> publishFilterChanged());
    }

    @FXML
    private void onOperatorChanged() {
        publishFilterChanged();
    }

    private void publishFilterChanged() {
        Set<Long> selectedTagIds = new HashSet<>();

        TreeItem<TagNodeViewModel> selected = tagTree.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getValue() != null && selected.getValue().id() != null) {
            selectedTagIds.add(selected.getValue().id());
        }

        TagFilterOperator operator = andRadio != null && andRadio.isSelected()
                ? TagFilterOperator.AND
                : TagFilterOperator.OR;

        eventPublisher.publishEvent(new TagFilterChangedEvent(Set.copyOf(selectedTagIds), operator));
    }

    public record TagNodeViewModel(Long id, String name, String colorHex) {
        @Override
        public String toString() {
            return name;
        }
    }
}
