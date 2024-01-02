package com.tyron.code.project.model.module;

import com.tyron.code.project.model.ProjectError;

import java.util.List;

/**
 * A module that has errors.
 */
public interface ErroneousModule extends Module {

    List<ProjectError> getErrors();
}
