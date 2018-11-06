package Starter;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class lightSwitchAction extends AbstractAction{

	private int mode = 1;
	
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
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
	
	public int lightMode()
	{
		return mode;
	}

}
