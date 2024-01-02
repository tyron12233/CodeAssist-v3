package com.tyron.code.project.impl;

import com.google.common.collect.ImmutableMap;
import com.tyron.code.info.JvmClassInfo;
import com.tyron.code.info.SourceClassInfo;
import com.tyron.code.info.builder.FileInfoBuilder;
import com.tyron.code.info.builder.JvmClassInfoBuilder;
import com.tyron.code.info.builder.SourceClassInfoBuilder;
import com.tyron.code.logging.Logging;
import com.tyron.code.project.InitializationException;
import com.tyron.code.project.impl.model.JarModuleImpl;
import com.tyron.code.project.impl.model.JavaModuleImpl;
import com.tyron.code.project.impl.model.JdkModuleImpl;
import com.tyron.code.project.model.JavaFileInfo;
import com.tyron.code.project.model.module.JdkModule;
import com.tyron.code.project.model.module.Module;
import com.tyron.code.project.util.PathUtils;
import com.tyron.code.project.util.StringSearch;
import com.tyron.code.project.util.Unchecked;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

public class ModuleInitializer {

    private static final Logger logger = Logging.get(ModuleInitializer.class);

    public void initializeModules(List<Module> modules) {
        modules.forEach(this::initializeModule);
    }

    public void initializeModule(Module module) {
        try {
            if (module instanceof JavaModuleImpl project) {
                initializeJavaProject(project);
                return;
            }

            if (module instanceof JarModuleImpl jarModule) {
                initializeJarModule(jarModule);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void initializeJavaProject(JavaModuleImpl project) {
        if (!Files.exists(project.getSourceDirectory())) {
            logger.warn("Source directory does not exist for project: {}", project.getName());
            return;
        }

        // add regular files to the project
        try (var stream = Unchecked.get(() -> Files.walk(project.getRootDirectory()))) {
            stream.filter(Files::isRegularFile)
                    .filter(it -> !it.getFileName().toString().endsWith(".java"))
                    .map(FileInfoBuilder::new)
                    .map(FileInfoBuilder::build)
                    .forEach(((JavaModuleImpl) project)::addFile);
        }

        logger.debug("Initializing java project: {}", project.getName());
        ImmutableMap<String, Consumer<Path>> handlers =
                ImmutableMap.of(
                        ".java",
                        path -> addOrUpdateFile(project, path)
                );
        PathUtils.walkDirectory(project.getSourceDirectory(), handlers, it -> false);
        logger.debug("Finished initializing java project: {}", project.getName());

        JdkModule jdkModule = project.getJdkModule();
        if (jdkModule != null) {
            Instant start = Instant.now();
            logger.debug("Initializing jdk module: {}", jdkModule.getName());

            List<JvmClassInfo> jvmClasses = getJvmClasses(jdkModule.getPath());
            jvmClasses.forEach(it -> ((JdkModuleImpl) jdkModule).addClass(it));

            Duration duration = Duration.between(start, Instant.now());
            logger.debug("Initialized {} classes, took {} ms", jdkModule.getClasses().size(), duration.toMillis());
        }
    }

    private void addOrUpdateFile(JavaModuleImpl module, Path path) {

        SourceClassInfoBuilder sourceClassInfoBuilder = new SourceClassInfoBuilder(path);
        SourceClassInfo build = sourceClassInfoBuilder.build();
        module.addClass(build);
    }

    private void initializeJarModule(JarModuleImpl jarModule) throws IOException {
        logger.debug("Initializing jar module: {}", jarModule.getName());
        List<JvmClassInfo> jvmClasses = getJvmClasses(jarModule.getPath());
        jvmClasses.forEach(jarModule::addClass);
        logger.debug("Finished initializing jar module: {}", jarModule.getName());
    }

    public static List<JvmClassInfo> getJvmClasses(Path jar) {
        Path jarRoot = PathUtils.getRootPathForJarFile(jar);
        try (var list = Files.walk(jarRoot)) {
            return list
                    .filter(it -> !Files.isDirectory(it))
                    .filter(it -> it.getFileName().toString().endsWith(".class"))
                    .map(it -> Unchecked.get(() -> Files.readAllBytes(it)))
                    .map(JvmClassInfoBuilder::new)
                    .map(JvmClassInfoBuilder::build)
                    .toList();
        } catch (IOException e) {
            throw new InitializationException(e.getMessage());
        } finally {
            Unchecked.run(() -> jarRoot.getFileSystem().close());
        }
    }
}
