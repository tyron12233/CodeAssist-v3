package com.tyron.code.project.model.module;

import com.tyron.code.project.model.ProjectError;

import java.util.List;

public interface ErroneousRootModule extends RootModule {

    List<ProjectError> getErrors();
}
