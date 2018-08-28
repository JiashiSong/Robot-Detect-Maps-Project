
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Listeners {

	private UI ui;
	
	public MouseAdapter[] DIRECTIONAL_LISTENERS;
	public MouseAdapter[] MENU_LISTENERS;
	public MouseAdapter MODE_LISTENER;
	
	public boolean pressed;
	
	public Listeners(final UI ui) {
		this.ui = ui;
		pressed = false;
		
		this.DIRECTIONAL_LISTENERS = new MouseAdapter[4];
		this.DIRECTIONAL_LISTENERS[DIRECTIONAL_BUTTONS.UP_BUTTON.ordinal()] = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if(ui.isConnected() && !pressed) {
					ui.getController().upPress();
					ui.addToLog("Forward\n");
					pressed = true;
				}
			}
			public void mouseReleased(MouseEvent e) { 
				if(ui.isConnected()) {
					ui.getController().upRelease();
					pressed = false;
				}
			}
		};
		this.DIRECTIONAL_LISTENERS[DIRECTIONAL_BUTTONS.DOWN_BUTTON.ordinal()] = new MouseAdapter() {
			public void mousePressed(MouseEvent e) { 
				if(ui.isConnected() && !pressed) {
					ui.getController().downPress();
					ui.addToLog("Backward\n");
					pressed = true;
				}
			}
			public void mouseReleased(MouseEvent e) { 
				if(ui.isConnected()) {
					ui.getController().downRelease();
					pressed = false;
				}
			}
		};
		this.DIRECTIONAL_LISTENERS[DIRECTIONAL_BUTTONS.LEFT_BUTTON.ordinal()] = new MouseAdapter() {
			public void mousePressed(MouseEvent e) { 
				if(ui.isConnected() && !pressed) {
					ui.getController().leftPress();
					ui.addToLog("Left\n");
					pressed = true;
				}
			}
			public void mouseReleased(MouseEvent e) 
			{ 
				if(ui.isConnected()) {
					ui.getController().leftRelease();
					pressed = false;
				}
			}
		};
		this.DIRECTIONAL_LISTENERS[DIRECTIONAL_BUTTONS.RIGHT_BUTTON.ordinal()] = new MouseAdapter() {
			public void mousePressed(MouseEvent e) { 
				if(ui.isConnected() && !pressed) {
					ui.getController().rightPress();
					ui.addToLog("Right\n");
					pressed = true;
				}
			}
			public void mouseReleased(MouseEvent e) { 
				if(ui.isConnected()) {
					ui.getController().rightRelease();
					pressed = false;
				}
			}
		};
		
		this.MODE_LISTENER = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				ui.getController().toggleControlMode();
			}
		};
	}
}
