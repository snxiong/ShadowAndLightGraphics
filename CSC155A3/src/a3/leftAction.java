package a3;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class leftAction extends AbstractAction{

	private static double amount = 1.0;
	Camera Cam;
	
	public leftAction(Camera oldCamera)
	{
		Cam = oldCamera;
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		amount = -1.0;
		Cam.setCamX(amount);
		//System.out.println("SUCESS");
	}


}
