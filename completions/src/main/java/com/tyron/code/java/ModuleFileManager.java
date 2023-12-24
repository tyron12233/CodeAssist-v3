package com.tyron.code.java;

import com.google.common.io.Files;
import com.tyron.code.java.parsing.MethodBodyPruner;
import com.tyron.code.java.parsing.ParserContext;
import com.tyron.code.project.file.FileManager;
import com.tyron.code.project.file.FileSnapshot;
import com.tyron.code.project.model.JarModule;
import com.tyron.code.project.model.ProjectModule;
import com.tyron.code.project.model.UnparsedJavaFile;
import com.tyron.code.project.util.JarReader;
import com.tyron.code.project.util.ModuleUtils;
import com.tyron.code.project.util.StringSearch;
import shadow.com.sun.tools.javac.api.JavacTool;
import shadow.com.sun.tools.javac.file.JavacFileManager;
import shadow.com.sun.tools.javac.file.Locations;
import shadow.com.sun.tools.javac.file.PathFileObject;
import shadow.com.sun.tools.javac.tree.JCTree;
import shadow.javax.tools.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private final ProjectModule module;


    public ModuleFileManager(FileManager projectFileManager, ProjectModule module) {
        super(createDelegateFileManager());
        this.projectFileManager = projectFileManager;
        this.module = module;

        try {
            JarModule jdkModule = module.getJdkModule();
            fileManager.setLocation(StandardLocation.PLATFORM_CLASS_PATH, List.of(jdkModule.getJarPath().toFile()));
            fileManager.setLocation(StandardLocation.SOURCE_PATH, List.of(module.getDirectory().toFile()));
            fileManager.setLocation(StandardLocation.CLASS_PATH, getClassPath(module));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Iterable<? extends File> getClassPath(ProjectModule module) {
        return ModuleUtils.getCompileClassPath(module).stream()
                .map(Path::toFile)
                .toList();

    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
        if (location == StandardLocation.SOURCE_PATH) {
            List<String> qualifiers = packageName.isEmpty()
                    ? List.of()
                    : Arrays.stream(packageName.split("\\.")).toList();
            return ModuleUtils.getFiles(qualifiers, module).stream()
                    .map(this::asSourceFileObject)
                    ::iterator;
        }

        // javac modules hack: on JDK 9 and above, javac will attempt to use the module
        // system but in android we don't use modules, this method will redirect modules into the
        // android.jar we provided in the classpath
        if (location.getClass().toString().contains("Module")) {
            return module.getJdkModule().getPackage(Arrays.stream(packageName.split("\\.")).toList()).stream()
                    .flatMap(packageScope -> packageScope.getFiles().stream())
                    .map(c -> new ClassFileObject(c.path(), JavaFileObject.Kind.CLASS))
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

    private JavaFileObject asSourceFileObject(UnparsedJavaFile unparsedJavaFile) {
        Path path = unparsedJavaFile.path();


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

        private final Path jarPath;
        private final Path pathInside;
        protected ClassFileObject(Path pathInside, Kind kind) {
            super(getRootFromJar(pathInside).toUri(), kind);

            this.jarPath = getRootFromJar(pathInside);
            this.pathInside = pathInside;
        }

        private static Path getRootFromJar(Path path) {
            return Paths.get(path.getFileSystem().toString());
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            throw new UnsupportedOperationException();
        }

        public String inferBinaryName(Iterable<? extends Path> paths) {
            return JarReader.getFqn(pathInside.toString());
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return java.nio.file.Files.newInputStream(pathInside);
        }
    }
}
