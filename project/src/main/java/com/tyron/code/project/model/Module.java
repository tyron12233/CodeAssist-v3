package com.tyron.code.project.model;

public class Module {


    private final ModuleType moduleType;
    private final String debugName;

    public Module(ModuleType moduleType) {
        this(moduleType, "unknown");
    }

    public Module(ModuleType moduleType, String debugName) {
        this.debugName = debugName;
        this.moduleType = moduleType;

    }

    public ModuleType getModuleType() {
        return moduleType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Module module = (Module) o;
        return  moduleType == module.moduleType && com.google.common.base.Objects.equal(debugName, module.debugName);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(moduleType, debugName);
    }

    @Override
    public String toString() {
        return "{ " + moduleType + ": " + debugName + " }";
    }


    public String getDebugName() {
        return debugName;
    }
}
