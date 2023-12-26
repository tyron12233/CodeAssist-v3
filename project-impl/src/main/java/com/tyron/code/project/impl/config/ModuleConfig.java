package com.tyron.code.project.impl.config;

import com.google.common.annotations.VisibleForTesting;
import red.jackf.tomlconfig.annotations.Config;

import java.util.ArrayList;
import java.util.List;

public class ModuleConfig implements Config {

    @Comment("The name that will be used when referencing this from another project")
    @Comment("Empty means use the name of the folder this config is located")
    public String name = "";

    @Comment("If this module contains child modules, it should be listed here")
    public List<String> includedModules = new ArrayList<>();

    @Comment("Defines the type of module this project will be")
    @Comment("Will determine what compiler to use")
    public ModuleType moduleType = ModuleType.DEFAULT;

    public List<Dependency> dependencies = new ArrayList<>();

    public enum ModuleType {
        DEFAULT,
        JAVA
    }

    @Transitive
    public static class Dependency {

        public DependencyScope scope = DependencyScope.IMPLEMENTATION;

        public String notation;

        public DependencyType type = DependencyType.MAVEN;

        public Dependency() {

        }

        @VisibleForTesting
        public Dependency(String notation) {
            this(DependencyScope.IMPLEMENTATION, notation, DependencyType.MAVEN);
        }

        @VisibleForTesting
        public Dependency(DependencyScope scope, String notation, DependencyType type) {
            this.scope = scope;
            this.notation = notation;
            this.type = type;
        }


        public enum DependencyScope {
            IMPLEMENTATION,
            RUNTIME_ONLY,
            COMPILE_ONLY
        }

        public enum DependencyType {
            FILE,
            MAVEN,
            PROJECT
        }
    }
}
