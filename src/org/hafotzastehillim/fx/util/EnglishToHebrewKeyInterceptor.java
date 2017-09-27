package org.hafotzastehillim.fx.util;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

public class EnglishToHebrewKeyInterceptor implements EventHandler<KeyEvent> {

	private boolean firstEvent = true;

	@Override
	public void handle(KeyEvent event) {
		if (!firstEvent) {
			firstEvent = true;
			return;
		}

		event.consume();

		firstEvent = false;
		KeyEvent.fireEvent(event.getTarget(),
				new KeyEvent(event.getSource(), event.getTarget(), event.getEventType(),
						EnglishToHebrew.convert(event.getCharacter()), event.getText(), event.getCode(),
						event.isShiftDown(), event.isControlDown(), event.isAltDown(), event.isMetaDown()));

	}

}
