package com.tyron.code.desktop.ui.control.richtext.syntax;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.tyron.code.project.util.StringUtil.cutOffAtFirst;
import static com.tyron.code.project.util.StringUtil.shortenPath;


/**
 * Rule-sets to pattern-match languages for syntax highlighting.
 *
 * @author Matt Coley
 * @see RegexSyntaxHighlighter Highlighter implementation that accepts these rule-sets.
 * @see LanguageStylesheets For retrieving stylesheets of the supported languages.
 */
public class RegexLanguages {
	private static final JsonMapper MAPPER = new JsonMapper();
	private static final Map<String, RegexRule> NAME_TO_LANG = new HashMap<>();
	private static final RegexRule LANG_JAVA;
	private static final RegexRule LANG_XML;
	private static final RegexRule LANG_ENGIMA_MAP;

	// Prevent construction
	private RegexLanguages() {
	}

	static {
		try {
			LANG_JAVA = addLanguage("/syntax/java.json");
			LANG_XML = addLanguage("/syntax/xml.json");
			LANG_ENGIMA_MAP = addLanguage("/syntax/enigma.json");
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to read syntax rules from resources", ex);
		}
	}

	/**
	 * Adds support for the language outlined by the contents of the given file.
	 * File path should point to a json file modeling {@link RegexRule}.
	 *
	 * @param path
	 * 		Path to regex json file. The language name is the file name, without the extension.
	 * 		<ul>
	 * 		    <li>Internal paths start with {@code /}.</li>
	 * 		    <li>External paths should exist if checked by {@code new File(path).exists()}.</li>
	 * 		</ul>
	 *
	 * @return Language root rule.
	 *
	 * @throws IOException
	 * 		When the file cannot be read or mapped into the rule structure.
	 */
	@NotNull
	public static RegexRule addLanguage(@NotNull String path) throws IOException {
		InputStream stream = RegexLanguages.class.getResourceAsStream(path);
		if (stream == null)
			stream = new FileInputStream(path);
		String name = cutOffAtFirst(shortenPath(path), ".");
		return addLanguage(name, stream);
	}

	/**
	 * Adds support for the language.
	 *
	 * @param name
	 * 		Language name to use as a key. Will be used in {@link #getLanguages()} and {@link #getLanguage(String)}.
	 * @param stream
	 * 		Input stream to file to parse.
	 *
	 * @return Language root rule.
	 *
	 * @throws IOException
	 * 		When the file cannot be read or mapped into the rule structure.
	 * @see LanguageStylesheets#addLanguage(String, String) You should assign a stylesheet by the same language name.
	 */
	@NotNull
	public static RegexRule addLanguage(@NotNull String name, @NotNull InputStream stream) throws IOException {
		JsonParser parser = MAPPER.createParser(new InputStreamReader(stream));
		RegexRule lang = parser.readValueAs(RegexRule.class);
		NAME_TO_LANG.put(name, lang);
		return lang;
	}

	/**
	 * @return Map of language names to language regex rule roots.
	 */
	@NotNull
	public static Map<String, RegexRule> getLanguages() {
		return Collections.unmodifiableMap(NAME_TO_LANG);
	}

	/**
	 * @param name
	 * 		Name of the language, matching the file path inside Recaf's {@code /syntax/} directory,
	 * 		without the {@code .json} extension. To get {@link #getJavaLanguage()} use {@code java}.
	 *
	 * @return Language regex rule root, or {@code null} if unknown language.
	 *
	 * @see LanguageStylesheets#addLanguage(String) Stylesheet to highlight the language with.
	 */
	@Nullable
	public static RegexRule getLanguage(@Nullable String name) {
		return NAME_TO_LANG.get(name);
	}

	/**
	 * @return Root rule for Java regex matching.
	 *
	 * @see LanguageStylesheets#getJavaStylesheet()
	 */
	@NotNull
	public static RegexRule getJavaLanguage() {
		return LANG_JAVA;
	}

	/**
	 * @return Root rule for XML regex matching.
	 *
	 * @see LanguageStylesheets#getXmlStylesheet()
	 */
	@NotNull
	public static RegexRule getXmlLanguage() {
		return LANG_XML;
	}

	/**
	 * @return Root rule for Engima regex matching.
	 *
	 * @see LanguageStylesheets#getEnigmaStylesheet()
	 */
	@NotNull
	public static RegexRule getLangEngimaMap() {
		return LANG_ENGIMA_MAP;
	}
}
