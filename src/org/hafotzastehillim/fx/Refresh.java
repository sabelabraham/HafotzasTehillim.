package org.hafotzastehillim.fx;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

public class Refresh extends StackPane {

	private BooleanProperty refreshing;
	private BooleanProperty alwaysVisible;

	private ObjectProperty<Duration> durationProperty;
	private ObjectProperty<Duration> delayProperty;
	private RotateTransition rotate;
	private PauseTransition pause;

	private SVGPath path;

	public Refresh(Duration duration, Duration delay) {
		getStyleClass().add("refresh");

		setMouseTransparent(true);

		path = new SVGPath();
		path.setContent("M12 6v3l4-4-4-4v3c-4.42 0-8 3.58-8 8 0 1.57.46 3.03 "
				+ "1.24 4.26L6.7 14.8c-.45-.83-.7-1.79-.7-2.8 0-3.31 "
				+ "2.69-6 6-6zm6.76 1.74L17.3 9.2c.44.84.7 1.79.7 2.8"
				+ " 0 3.31-2.69 6-6 6v-3l-4 4 4 4v-3c4.42 0 8-3.58 8-8 0-1.57-.46-3.03-1.24-4.26z");
		path.getStyleClass().add("icon");
		getChildren().add(path);

		durationProperty().set(duration);
		delayProperty().set(delay);

		rotate = new RotateTransition();
		rotate.durationProperty().bind(durationProperty());
		rotate.setNode(path);
		rotate.setFromAngle(0);
		rotate.setToAngle(180);
		rotate.setCycleCount(Animation.INDEFINITE);
		rotate.setInterpolator(Interpolator.LINEAR);

		pause = new PauseTransition();
		pause.durationProperty().bind(delayProperty());

		SequentialTransition both = new SequentialTransition(pause, rotate);

		refreshingProperty().addListener((obs, ov, nv) -> {
			if(nv) {
				both.playFromStart();
			} else {
				both.stop();
			}
		});

		path.visibleProperty().bind(rotate.statusProperty().isEqualTo(Animation.Status.RUNNING).or(alwaysVisibleProperty()));
	}

	public Refresh(Duration duration) {
		this(duration, Duration.ZERO);
	}

	public Refresh(int millisDuration, int millisDelay) {
		this(Duration.millis(millisDuration), Duration.millis(millisDelay));
	}

	public Refresh(int durationMillis) {
		this(durationMillis, 0);
	}

	public Refresh() {
		this(400);
	}

	public BooleanProperty refreshingProperty() {
		if (refreshing == null) {
			refreshing = new SimpleBooleanProperty(this, "refreshing");
		}

		return refreshing;
	}

	public final boolean isRefreshing() {
		return refreshingProperty().get();
	}

	public final void setRefreshing(boolean bool) {
		refreshingProperty().set(bool);
	}

	public BooleanProperty alwaysVisibleProperty() {
		if(alwaysVisible == null) {
			alwaysVisible = new SimpleBooleanProperty(this, "alwaysVisible", false);
		}

		return alwaysVisible;
	}

	public final boolean isAlwaysVisible() {
		return alwaysVisibleProperty().get();
	}

	public final void setAlwaysVisible(boolean flag) {
		alwaysVisibleProperty().set(flag);
	}

	public ObjectProperty<Duration> durationProperty() {
		if (durationProperty == null) {
			durationProperty = new SimpleObjectProperty<>(this, "duration");
		}

		return durationProperty;
	}

	public final Duration getDelay() {
		return delayProperty().get();
	}

	public final void setDelay(Duration d) {
		delayProperty().set(d);
	}
	public ObjectProperty<Duration> delayProperty() {
		if (delayProperty == null) {
			delayProperty = new SimpleObjectProperty<>(this, "delay");
		}

		return delayProperty;
	}

	public final Duration getDuration() {
		return durationProperty().get();
	}

	public final void setDuration(Duration d) {
		durationProperty().set(d);
	}

	public Interpolator getInterpolator() {
		return rotate.getInterpolator();
	}

	public void setInterpolator(Interpolator i) {
		rotate.setInterpolator(i);
	}
}
