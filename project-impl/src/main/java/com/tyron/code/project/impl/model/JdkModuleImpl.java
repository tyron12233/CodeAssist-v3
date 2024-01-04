package com.tyron.code.project.impl.model;

import com.tyron.code.project.ModuleManager;
import com.tyron.code.project.model.module.JdkModule;

import java.nio.file.Path;

public class JdkModuleImpl extends JarModuleImpl implements JdkModule {
    private final String jdkVersion;

    public JdkModuleImpl(ModuleManager moduleManager, Path jarPath, String jdkVersion) {
        super(moduleManager, jarPath);
        setName("JDK-" + jdkVersion);
        this.jdkVersion = jdkVersion;
    }

    @Override
    public String getJdkVersion() {
        return jdkVersion;
    }

    
}