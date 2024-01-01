package com.tyron.code.java;

import com.google.common.io.Files;
import com.tyron.code.info.ClassInfo;
import com.tyron.code.info.JvmClassInfo;
import com.tyron.code.info.SourceClassInfo;
import com.tyron.code.java.parsing.MethodBodyPruner;
import com.tyron.code.java.parsing.ParserContext;
import com.tyron.code.project.file.FileManager;
import com.tyron.code.project.file.FileSnapshot;
import com.tyron.code.project.model.JavaFileInfo;
import com.tyron.code.project.model.module.JavaModule;
import com.tyron.code.project.model.module.JdkModule;
import com.tyron.code.project.util.ClassNameUtils;
import com.tyron.code.project.util.ModuleUtils;
import com.tyron.code.project.util.PathUtils;
import com.tyron.code.project.util.StringSearch;
import shadow.com.sun.tools.javac.api.JavacTool;
import shadow.com.sun.tools.javac.file.JavacFileManager;
import shadow.com.sun.tools.javac.tree.JCTree;
import shadow.javax.tools.*;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

public class ModuleFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

    private static final Logger LOG = Logger.getLogger("main");
    private Path completingFile;
    private String completingContents;

    private static StandardJavaFileManager createDelegateFileManager() {
        var compiler = JavacTool.create();
        return compiler.getStandardFileManager(ModuleFileManager::logError, null, Charset.defaultCharset());
    }

    private static void logError(Diagnostic<?> error) {
        LOG.warning(error.getMessage(null));
    }

    private final FileManager projectFileManager;
    private final JavaModule module;


    public ModuleFileManager(FileManager projectFileManager, JavaModule module) {
        super(createDelegateFileManager());
        this.projectFileManager = projectFileManager;
        this.module = module;

        try {
            JdkModule jdkModule = module.getJdkModule();
            fileManager.setLocation(StandardLocation.PLATFORM_CLASS_PATH, List.of(jdkModule.getPath().toFile()));
            fileManager.setLocation(StandardLocation.SOURCE_PATH, List.of(module.getSourceDirectory().toFile()));
            fileManager.setLocation(StandardLocation.CLASS_PATH, getClassPath(module));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Iterable<? extends File> getClassPath(JavaModule module) {
        return ModuleUtils.getCompileClassPath(module).stream()
                .map(Path::toFile)
                .toList();

    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
        if (location == StandardLocation.SOURCE_PATH) {
            return ModuleUtils.getFiles(packageName, module).stream()
                    .map(this::asSourceFileObject)
                    ::iterator;
        }

        // javac modules hack: on JDK 9 and above, javac will attempt to use the module
        // system but in android we don't use modules, this method will redirect modules into the
        // android.jar we provided in the classpath
        if (location.getClass().toString().contains("Module")) {
            return module.getJdkModule().getClasses().stream()
                    .filter(it -> packageName.replace('.', '/').equals(it.getPackageName()))
                    .map(c -> new ClassFileObject(module.getJdkModule().getPath(), c))
                    .map(c -> (JavaFileObject) c)
                    .toList();
        }
        return super.list(location, packageName, kinds, recurse);
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        if (location == StandardLocation.SOURCE_PATH) {
            return extractClassName((FileSnapshot) file);
        }

        if (!(file instanceof ClassFileObject)) {
            return super.inferBinaryName(location, file);
        }

        Objects.requireNonNull(file);
        Iterable<? extends Path> path = getLocationAsPaths(location);
        if (path == null) {
            return null;
        }

        return ((ClassFileObject) file).inferBinaryName(path);
    }

    @SuppressWarnings("unchecked")
    private Iterable<? extends Path> getLocationAsPaths(Location location) {
        try {
            Method getLocationAsPaths = JavacFileManager.class.getDeclaredMethod("getLocationAsPaths", Location.class);
            getLocationAsPaths.setAccessible(true);
            Object invoke = getLocationAsPaths.invoke(this.fileManager, location);
            return (Iterable<? extends Path>) invoke;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private String extractClassName(FileSnapshot source) {
        return Optional.of(source)
                .map(fs -> StringSearch.packageName(String.valueOf(fs.getCharContent(true))))
                .filter(pkg -> !pkg.isEmpty())
                .map(pkg -> String.format("%s.%s", pkg, Files.getNameWithoutExtension(source.getName())))
                .orElseGet(() -> Files.getNameWithoutExtension(source.getName()));
    }

    private JavaFileObject asSourceFileObject(ClassInfo javaFileInfo) {
        if (!(javaFileInfo instanceof SourceClassInfo sourceClassInfo)) {
            throw new IllegalArgumentException("Expected source class info");
        }
        Path path = sourceClassInfo.getPath();


        String content = Optional.ofNullable(completingFile.equals(path) ? completingFile : null)
                .map(it -> completingContents)
                .orElse(projectFileManager.getFileContent(path)
                        .map(contents -> pruneMethodBodiesIfNeeded(path, contents))
                        .orElse("").toString());

        return FileSnapshot.create(path.toUri(), content);
    }

    private CharSequence pruneMethodBodiesIfNeeded(Path path, CharSequence content) {
        if (!projectFileManager.isFileOpen(path)) {
            ParserContext context = new ParserContext();
            JCTree.JCCompilationUnit unit = context.parse(path.getFileName().toString(), content);
            MethodBodyPruner methodBodyPruner = new MethodBodyPruner();
            return methodBodyPruner.translate(unit).toString();
        }

        return content;
    }

    public void setCompletingFile(Path path, String contents) {

        this.completingFile = path;
        this.completingContents = contents;
    }

    public void clearCompletingFile() {
        this.completingFile = null;
        this.completingContents = null;
    }

    private static class ClassFileObject extends SimpleJavaFileObject {
        private final Path pathInside;
        private final JvmClassInfo c;

        protected ClassFileObject(Path jarPath, JvmClassInfo c) {
            super(jarPath.toUri(), Kind.CLASS);
            this.pathInside = jarPath;
            this.c = c;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            throw new UnsupportedOperationException();
        }

        public String inferBinaryName(Iterable<? extends Path> paths) {
            return c.getName().replace('/', '.');
        }

        @Override
        public InputStream openInputStream() {
            return new ByteArrayInputStream(c.getBytecode());
        }
    }
}
