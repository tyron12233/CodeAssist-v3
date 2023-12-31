package com.tyron.code.desktop.ui.control.richtext.linegraphics;

import com.tyron.code.desktop.ui.control.richtext.Editor;
import com.tyron.code.project.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.fxmisc.richtext.CodeArea;

/**
 * Graphic factory to draw line numbers.
 *
 * @author Matt Coley
 */
public class LineNumberFactory extends AbstractLineGraphicFactory {
	private CodeArea codeArea;

	/**
	 * New line number factory.
	 */
	public LineNumberFactory() {
		super(P_LINE_NUMBERS);
	}

	@Override
	public void install(@NotNull Editor editor) {
		codeArea = editor.getCodeArea();
	}

	@Override
	public void uninstall(@NotNull Editor editor) {
		codeArea = null;
	}

	@Override
	public void apply(@NotNull LineContainer container, int paragraph) {
		if (codeArea == null) return;

		Label label = new Label(format(paragraph + 1, computeDigits(codeArea.getParagraphs().size())));
		HBox.setHgrow(label, Priority.ALWAYS);
		container.addHorizontal(label);
	}

	private static String format(int line, int digits) {
		return String.format(StringUtil.fillLeft(digits, " ", String.valueOf(line)));
	}

	private static int computeDigits(int size) {
		return (int) Math.floor(Math.log10(size)) + 1;
	}
}
