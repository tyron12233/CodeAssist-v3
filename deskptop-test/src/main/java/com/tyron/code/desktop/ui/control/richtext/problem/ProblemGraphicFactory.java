package com.tyron.code.desktop.ui.control.richtext.problem;

import com.tyron.code.desktop.ui.control.FontIconView;
import com.tyron.code.desktop.ui.control.richtext.Editor;
import com.tyron.code.desktop.ui.control.richtext.linegraphics.AbstractLineGraphicFactory;
import com.tyron.code.desktop.ui.control.richtext.linegraphics.LineContainer;
import com.tyron.code.desktop.ui.control.richtext.linegraphics.LineGraphicFactory;
import org.jetbrains.annotations.NotNull;
import javafx.scene.Cursor;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

/**
 * Graphic factory that adds overlays to line graphics indicating the problem status of the line.
 *
 * @author Matt Coley
 * @see ProblemTracking
 */
public class ProblemGraphicFactory extends AbstractLineGraphicFactory {
	private Editor editor;

	/**
	 * New graphic factory.
	 */
	public ProblemGraphicFactory() {
		super(LineGraphicFactory.P_LINE_PROBLEMS);
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
		ProblemTracking problemTracking = editor.getProblemTracking();

		// Always null if no bracket tracking is registered for the editor.
		if (problemTracking == null) return;

		// Add problem graphic overlay to lines with problems.
		int line = paragraph + 1;
		Problem problem = problemTracking.getProblem(line);
		if (problem != null) {
			Rectangle shape = new Rectangle();
			shape.widthProperty().bind(container.widthProperty());
			shape.heightProperty().bind(container.heightProperty());
			shape.setCursor(Cursor.HAND);
			shape.setFill(Color.RED);
			shape.setOpacity(0.33);

			Tooltip tooltip = new Tooltip(formatTooltipMessage(problem));
			tooltip.setGraphic(new FontIconView(CarbonIcons.ERROR, Color.RED));
			tooltip.getStyleClass().add("error-text");
			Tooltip.install(shape, tooltip);
			container.addTopLayer(shape);
		}
	}

	private static String formatTooltipMessage(Problem problem) {
		StringBuilder sb = new StringBuilder();
		int line = problem.getLine();
		if (line > 0) {
			int column = problem.getColumn();
			if (column >= 0) {
				sb.append("Column ").append(column);
			}
		} else {
			sb.append("Unknown line");
		}
		return sb.append('\n').append(problem.getMessage()).toString();
	}
}