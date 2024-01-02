package com.tyron.code.project.impl.model;

import com.tyron.code.project.model.module.JdkModule;

import java.nio.file.Path;

public class JdkModuleImpl extends JarModuleImpl implements JdkModule {
    private final String jdkVersion;

    public JdkModuleImpl(Path jarPath, String jdkVersion) {
        super(jarPath);
        setName("JDK-" + jdkVersion);
        this.jdkVersion = jdkVersion;
    }

    @Override
    public String getJdkVersion() {
        return jdkVersion;
    }

    
}