package com.crm.personal.domain.media.port;

import com.crm.personal.domain.media.model.StoredMediaFile;

import java.nio.file.Path;

public interface MediaStoragePort {

    StoredMediaFile store(Path sourceFile);

    Path resolve(String relativePath);
}
