package org.hafotzastehillim.fx;

import com.jfoenix.controls.JFXListCell;

import javafx.beans.binding.When;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableNumberValue;
import javafx.css.PseudoClass;
import javafx.scene.control.ListCell;
import javafx.scene.shape.Rectangle;

public class RoundEdgedListCell<T> extends ListCell<T> {

	private static final PseudoClass first = PseudoClass.getPseudoClass("first");
	private static final PseudoClass last = PseudoClass.getPseudoClass("last");
	private static final PseudoClass single = PseudoClass.getPseudoClass("single");

	private IntegerProperty itemSize;
	private ReadOnlyBooleanWrapper isFirst;
	private ReadOnlyBooleanWrapper isLast;
	private ReadOnlyBooleanWrapper isSingle;

	public RoundEdgedListCell(ObservableNumberValue itemSize) {
		this.itemSize = new SimpleIntegerProperty();
		this.itemSize.bind(itemSize);

		getStyleClass().add("round-edged-list-cell");

		isFirst = new ReadOnlyBooleanWrapper();
		isFirst.addListener((obs) -> pseudoClassStateChanged(first, isFirst.get()));
		isFirst.bind(indexProperty().isEqualTo(0).and(this.itemSize.greaterThan(1)));

		isLast = new ReadOnlyBooleanWrapper();
		isLast.addListener((obs) -> pseudoClassStateChanged(last, isLast.get()));
		isLast.bind(indexProperty().isEqualTo(this.itemSize.subtract(1)).and(this.itemSize.greaterThan(1)));

		isSingle = new ReadOnlyBooleanWrapper();
		isSingle.addListener((obs) -> pseudoClassStateChanged(single, isSingle.get()));
		isSingle.bind(indexProperty().isEqualTo(0).and(this.itemSize.isEqualTo(1)));
	}

	public ReadOnlyBooleanProperty isFirstProperty() {
		return isFirst.getReadOnlyProperty();
	}

	public ReadOnlyBooleanProperty isLastProperty() {
		return isLast.getReadOnlyProperty();
	}

	public ReadOnlyBooleanProperty isSingleProperty() {
		return isSingle.getReadOnlyProperty();
	}

}
