package org.hafotzastehillim.pointentry.fx;

import fxlauncher.UIProvider;
import fxlauncher.FXManifest;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Splash implements UIProvider {

	private VBox image;
	private Label label;
	private ProgressBar progress;

	@Override
	public Parent createLoader() {
		ImageView view = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/logo.png")));
		view.setFitWidth(200);
		view.setFitHeight(250);

		label = new Label("Updating...");
		progress = new ProgressBar();
		progress.setPrefWidth(view.getFitWidth() - 20);

		VBox update = new VBox(label, progress);
		update.setPadding(new Insets(10));
		update.visibleProperty().bind(progress.progressProperty().greaterThanOrEqualTo(0.0));

		image = new VBox(view, update);

		return image;
	}

	@Override
	public Parent createUpdater(FXManifest manifest) {
		return image;
	}

	@Override
	public void init(Stage stage) {

	}

	@Override
	public void updateProgress(double prog) {
		progress.setProgress(prog);
	}
}
