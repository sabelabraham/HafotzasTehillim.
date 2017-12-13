package org.hafotzastehillim.pointentry.fx.cell;

import com.jfoenix.controls.JFXListCell;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.When;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableNumberValue;
import javafx.css.PseudoClass;
import javafx.scene.shape.Rectangle;

public class RoundEdgedJFXListCell<T> extends JFXListCell<T> {

	private static final PseudoClass first = PseudoClass.getPseudoClass("first");
	private static final PseudoClass last = PseudoClass.getPseudoClass("last");
	private static final PseudoClass single = PseudoClass.getPseudoClass("single");

	private double radii;
	private IntegerProperty itemSize;
	private ReadOnlyBooleanWrapper isFirst;
	private ReadOnlyBooleanWrapper isLast;
	private ReadOnlyBooleanWrapper isSingle;

	public RoundEdgedJFXListCell(double radii, ObservableNumberValue itemSize) {
		this.radii = radii;
		this.itemSize = new SimpleIntegerProperty();
		this.itemSize.bind(itemSize);

		getStyleClass().addAll("round-edged-list-cell", "round-edged-jfx-list-cell");

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

	@Override
	public void updateItem(T t, boolean selected) {
		super.updateItem(t, selected);

		Rectangle clip = new Rectangle();
		clip.setArcWidth(radii * 2);
		clip.setArcHeight(radii * 2);
		clip.xProperty().bind(translateXProperty());
		clip.yProperty().bind(new When(isFirst.or(isSingle)).then(translateYProperty())
				.otherwise(translateYProperty().subtract(radii)));
		clip.widthProperty().bind(widthProperty());
		clip.heightProperty()
				.bind(Bindings.when(isFirst).then(heightProperty().add(radii))
						.otherwise(Bindings.when(isSingle).then(heightProperty())
								.otherwise(Bindings.when(isLast).then(heightProperty().add(radii))
										.otherwise(heightProperty().add(radii).multiply(2)))));

		cellRippler.setClip(clip);

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
