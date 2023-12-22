package com.tyron.code.java.parsing;

import com.tyron.code.java.SourceFileObject;
import shadow.com.sun.tools.javac.file.JavacFileManager;
import shadow.com.sun.tools.javac.parser.JavacParser;
import shadow.com.sun.tools.javac.parser.ParserFactory;
import shadow.com.sun.tools.javac.parser.Scanner;
import shadow.com.sun.tools.javac.parser.ScannerFactory;
import shadow.com.sun.tools.javac.tree.JCTree;
import shadow.com.sun.tools.javac.util.Context;
import shadow.com.sun.tools.javac.util.Log;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;

import static com.google.common.base.Charsets.UTF_8;

/**
 * Environment for using Javac Parser
 */
public class ParserContext {
    private final Context javacContext;
    private final JavacFileManager javacFileManager;

    public ParserContext() {
        javacContext = new Context();
        javacFileManager = new JavacFileManager(javacContext, true /* register */, UTF_8);
    }

    /**
     * Set source file of the log.
     *
     * <p>This method should be called before parsing or lexing. If not set, IllegalArgumentException
     * will be thrown if the parser encounters errors.
     */
    public void setupLoggingSource(String filename) {
        SourceFileObject sourceFileObject = new SourceFileObject(Paths.get(filename));
        Log javacLog = Log.instance(javacContext);
        javacLog.setWriters(new PrintWriter(OutputStream.nullOutputStream()));
        javacLog.useSource(sourceFileObject);
    }

    /**
     * Parses the content of a Java file.
     *
     * @param filename the filename of the Java file
     * @param content the content of the Java file
     */
    public JCTree.JCCompilationUnit parse(String filename, CharSequence content) {
        setupLoggingSource(filename);

        // Create a parser and start parsing.
        JavacParser parser =
                ParserFactory.instance(javacContext)
                        .newParser(
                                content, true /* keepDocComments */, true /* keepEndPos */, true /* keepLineMap */);
        return parser.parseCompilationUnit();
    }

    public Scanner tokenize(CharSequence content, boolean keepDocComments) {
        return ScannerFactory.instance(javacContext).newScanner(content, keepDocComments);
    }
}
