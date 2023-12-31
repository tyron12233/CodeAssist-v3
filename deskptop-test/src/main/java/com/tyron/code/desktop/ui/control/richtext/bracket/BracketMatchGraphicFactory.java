package com.tyron.code.desktop.ui.control.richtext.bracket;

import com.tyron.code.desktop.ui.control.richtext.Editor;
import com.tyron.code.desktop.ui.control.richtext.linegraphics.AbstractLineGraphicFactory;
import com.tyron.code.desktop.ui.control.richtext.linegraphics.LineContainer;
import org.jetbrains.annotations.NotNull;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.DepthTest;
import javafx.scene.control.Separator;
import javafx.scene.layout.StackPane;

/**
 * Graphic factory that adds a line indicator to matched lines containing the
 * {@link SelectedBracketTracking#getRange() current bracket pair}.
 *
 * @author Matt Coley
 * @see SelectedBracketTracking
 */
public class BracketMatchGraphicFactory extends AbstractLineGraphicFactory {
	private Editor editor;

	/**
	 * New graphic factory.
	 */
	public BracketMatchGraphicFactory() {
		super(P_BRACKET_MATCH);
	}

	@Override
	public void install(@NotNull Editor editor) {
		this.editor = editor;
	}

	@Override
	public void uninstall(@NotNull Editor editor) {
		this.editor = null;
	}


	@Override
	public void apply(@NotNull LineContainer container, int paragraph) {
		SelectedBracketTracking selectedBracketTracking = editor.getSelectedBracketTracking();

		// Always null if no bracket tracking is registered for the editor.
		if (selectedBracketTracking == null) return;

		// Add brace line for selected.
		if (selectedBracketTracking.isSelectedParagraph(paragraph)) {
			SelectedLineSeparator separator = new SelectedLineSeparator();
			StackPane.setAlignment(separator, Pos.CENTER_RIGHT);
			container.addTopLayer(separator);
		}
	}

	static class SelectedLineSeparator extends Separator {
		private SelectedLineSeparator() {
			super(Orientation.VERTICAL);
			getStyleClass().add("matched-brace-line");
			setPadding(new Insets(0));
		}

		@Override
		protected void setWidth(double value) {
			super.setWidth(value);
			// Keeps the vertical separator 'inline' with the parent container's edge
			setTranslateX(-value);
		}

		@Override
		protected void setHeight(double value) {
			super.setHeight(value + 1);
		}
	}
}
