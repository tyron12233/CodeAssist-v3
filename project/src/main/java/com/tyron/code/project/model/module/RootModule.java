package com.tyron.code.project.model.module;

import java.util.List;

public interface RootModule extends Module {

    List<Module> getIncludedModules();
}
