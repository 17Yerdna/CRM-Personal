package com.crm.personal.domain.tag.port;

import com.crm.personal.domain.tag.model.Tag;
import com.crm.personal.domain.tag.model.TagId;

import java.util.List;
import java.util.Optional;

public interface TagRepositoryPort {

    List<Tag> findAll();

    Optional<Tag> findById(TagId id);

    Tag save(Tag tag);
    void deleteById(TagId id);
}
