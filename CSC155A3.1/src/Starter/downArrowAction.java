package Starter;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class downArrowAction extends AbstractAction{

	private static float amount = 0.0f;
	private boolean active = false;
	protected int mode = 2;
	Camera Cam;
	
	public downArrowAction(Camera oldCamera)
	{
		Cam = oldCamera;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		amount = 2.0f;
		Cam.setMode(mode);
		active = true;
	}
	

}
