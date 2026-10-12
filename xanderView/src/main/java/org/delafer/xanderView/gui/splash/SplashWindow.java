package org.delafer.xanderView.gui.splash;


import org.delafer.xanderView.general.State;
import org.delafer.xanderView.sound.SoundBeep;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Shell;


public class SplashWindow extends BaseSplash{

    private State state;

   public SplashWindow(Shell parent, State state) {
       super(parent);
       this.state = state;
       this.show();
   }

	private int getColorConstant(State state) {
	       switch (state) {
	       case Success:
	    	   return SWT.COLOR_GREEN;
	       case Error:
	    	   return SWT.COLOR_RED;
	       case Special1:
	    	   return SWT.COLOR_YELLOW;
	       case Special2:
	    	   return SWT.COLOR_GRAY;
	       case Ignore:
	       default:
	    	   return SWT.COLOR_BLUE;
	       }
	}

    /**
     * custom drawler to show a content
     */
    protected void drawConent() {
        int width = shellBounds.width / 10;
        if (width<100) width = 100;

        int height = shellBounds.height / 10;
        if (height<100) height = 100;

        splash.setSize(width, height);

        Color color = display.getSystemColor(getColorConstant(state));
        splash.setBackground(color);
        // Move the dialog to the center of the top level shell.

        org.eclipse.swt.graphics.Point dialogSize = splash.getSize();

        splash.setLocation(
                shellBounds.x + (shellBounds.width - dialogSize.x) / 2,
                shellBounds.y + (shellBounds.height - dialogSize.y) / 2);

        if (State.Error.equals(state)) SoundBeep.beep();
    }
}
