package com.tyron.code.desktop.ui.control;

import org.jetbrains.annotations.NotNull;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;

/**
 * Support for tooltips with bindings to {@link Lang} for any supported control type.
 *
 * @author Matt Coley
 */
public interface Tooltipable {
	/**
	 * Implemented by {@link Control#setTooltip(Tooltip)}.
	 *
	 * @param tooltip
	 * 		Tooltip to assign.
	 */
	void setTooltip(Tooltip tooltip);

	/**
	 * @param tooltipKey
	 * 		Translation key for tooltip display.
	 *
	 * @return Self.
	 */
	@NotNull
	default <T extends Tooltipable> T withTooltip(@NotNull String tooltipKey) {
//		return withTooltip(Lang.getBinding(tooltipKey));
		throw new UnsupportedOperationException();
	}

	/**
	 * @param tooltipValue
	 * 		Text binding value for tooltip display.
	 *
	 * @return Self.
	 */
	@NotNull
	@SuppressWarnings("unchecked")
	default <T extends Tooltipable> T withTooltip(@NotNull ObservableValue<String> tooltipValue) {
		Tooltip tooltip = new Tooltip();
		tooltip.textProperty().bind(tooltipValue);
		setTooltip(tooltip);
		return (T) this;
	}
}