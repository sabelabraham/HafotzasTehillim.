package org.hafotzastehillim.pointentry.spreadsheet;

import java.io.IOException;
import java.util.function.Supplier;

import org.hafotzastehillim.pointentry.fx.Model;
import org.hafotzastehillim.pointentry.fx.util.DialogUtils;
import org.hafotzastehillim.pointentry.fx.util.Util;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginDialog extends Stage {

	private LoginController controller;
	private Pane root;
	private Rectangle clip;

	public LoginDialog(boolean cached) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/Login.fxml"));

		try {
			root = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		DialogUtils.setupStage(this);

		controller = loader.getController();
		if (cached)
			controller.setMessageByLoggedIn();

		initModality(Modality.APPLICATION_MODAL);

		root.getStyleClass().add("login-dialog");
		root.getStylesheets().add(getClass().getResource("/resources/css/login-dialog.css").toExternalForm());

		clip = new Rectangle();
		clip.widthProperty().bind(root.widthProperty().divide(2));
		clip.heightProperty().bind(root.heightProperty());

		root.setClip(clip);
		root.translateXProperty().bind(clip.translateXProperty().negate());

		controller.staySignedInProperty().bindBidirectional(Model.getInstance().staySignedInProperty());
		controller.setShowStaySignedIn(cached);

		setOnHidden(evt -> {
			SheetsAPI.stop();
		});

		Scene scene = new Scene(new Group(root));
		setScene(scene);

		setTitle("Google Login");
		setResizable(false);
	}

	public void setSignInAction(Supplier<Task<?>> supplier) {
		controller.setSignInAction(evt -> {
			Task<?> task = supplier.get();

			task.stateProperty().addListener((obs, ov, nv) -> {
				if (ov == State.RUNNING) {
					close(); // essentially passes back control to application
				}
			});

			Thread th = new Thread(task);
			th.setDaemon(true);
			th.start();
			
			Util.onceOnSucceeded(controller.getWebView().getEngine().getLoadWorker().stateProperty(), () -> {
				moveClipRight();
			});
		});
	}

	public void setCustomMessage(String msg) {
		controller.setCustomMessage(msg);

		if (msg == null || msg.isEmpty())
			setTitle("Google Login");
		else
			setTitle("Google Login | " + msg);

	}

	public void setWebAddress(String url) {
		Platform.runLater(() -> controller.getWebView().getEngine().load(url));
	}

	public void moveClipRight() {
		TranslateTransition animation = new TranslateTransition(Duration.seconds(0.25), clip);
		animation.setToX(root.getWidth() / 2);

		animation.play();
	}

	public void moveClipLeft() {
		TranslateTransition animation = new TranslateTransition(Duration.ONE, clip);
		animation.setToX(0);

		animation.play();
	}

}
