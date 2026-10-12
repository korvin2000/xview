package org.delafer.xanderView.gui.splash;


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


public abstract class BaseSplash {

   private int splashPos = 0;
   private final int SPLASH_MAX = 5;

   protected Shell splash;
   protected Display display;
   protected Rectangle shellBounds;

   public BaseSplash(Shell parent) {
      this.splash = new Shell(parent, SWT.ON_TOP | SWT.APPLICATION_MODAL);
      this.display  = Display.getCurrent();
      this.shellBounds = parent.getBounds();
   }

   public int getDelay() {
       return 40;
   }

   protected void show() {
       drawConent();
       splash.layout(true);
       splash.open();
       display.asyncExec(new Runnable()
       {
           public void run()
           {

               for(splashPos = 0; splashPos < SPLASH_MAX; splashPos++)
               {
                   try {

                       Thread.sleep(getDelay());
                   }
                   catch(InterruptedException e) {

                       e.printStackTrace();
                   }
               }
               //ApplicationLauncher.reportWindow.initWindow();
               splash.close();
           }
       });

       while(splashPos != SPLASH_MAX)
       {
           if(!display.readAndDispatch())
           {
               display.sleep();
           }
       }
   }

   protected abstract void drawConent();

}
