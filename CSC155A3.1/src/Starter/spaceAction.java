package Starter;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class spaceAction  extends AbstractAction{

	private int mode = 1;
	private double planet1Degree = 0.0;
	private double planet2Degree = 15.0;
	private double moon1Degree = 10.0;
	private double moon2Degree = 20.0;
	private double moon3Degree = 30.0;
	private double moon4Degree = 40.0;
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(mode == 0)
		{
			mode = 1;
		}
		else
		{
			mode = 0;
		}
	}
	
	public int axesMode()
	{
		return mode;
	}

}
