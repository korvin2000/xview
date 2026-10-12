package org.delafer.xanderView.gui.splash;


import org.delafer.xanderView.gui.config.ApplConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Shell;


public class SplashText extends BaseSplash{
        private String text;

   public SplashText(Shell parent, String text) {
       super(parent);
       this.text = prepare(text);
       this.show();
   }

    private String prepare(String text) {

       if (text.length()>18) {
           text = ".."+text.substring(text.length()-16);
       }
       text = text.replace('\\',';').replace('/',';');
       String[] chunks =  text.split(";");
       StringBuilder sb = new StringBuilder(text.length());
       int num = ApplConfiguration.instance().currentDir();
       sb.append(num == 0 ? "*" : num);
       for (int i = 1; i <= chunks.length; i++) {
            sb.append(' ');
            String chunk = chunks[i-1];
            sb.append(i<chunks.length?chunk.toLowerCase():chunk.toUpperCase());
       }


       return sb.toString();
    }

    /**
     * @return
     */
    @Override
    public int getDelay() {
        return 100;
    }

    /**
     * custom drawler to show a content
     */
    protected void drawConent() {


        int width = shellBounds.width / 3;
        if (width<100) width = 200;

        int height = shellBounds.height / 10;
        if (height<100) height = 64;

        splash.setSize(width, height);

        splash.setBackground(display.getSystemColor(getBackground()));
        // Move the dialog to the center of the top level shell.

        splash.open();
        GC gc = new GC(splash);
        Font boldFont = new Font(display, "Arial", width / 24, SWT.BOLD);
        gc.setFont(boldFont);
        gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
        String message = this.text;
        Point textSize = gc.textExtent(message);
//        System.out.println(String.format("Text size w,h: %s,%s", textSize.x, textSize.y));
//        System.out.println(String.format("Splash size w,h: %s,%s", splash.getSize().x, splash.getSize().y));
        int atx = (splash.getSize().x  - textSize.x)/2;
        int aty = (splash.getSize().y  - textSize.y)/2;
        gc.drawText(message, atx, aty, false);
        boldFont.dispose();
        gc.dispose();


        org.eclipse.swt.graphics.Point dialogSize = splash.getSize();

        splash.setLocation(
                shellBounds.x + (shellBounds.width - dialogSize.x) / 2,
                shellBounds.y + (shellBounds.height - dialogSize.y) / 2);
    }

    public int getBackground() {
        int dir = ApplConfiguration.instance().currentDir() % 10;
        return switch (dir) {
            case 1 -> SWT.COLOR_DARK_BLUE;
            case 2 -> SWT.COLOR_DARK_RED;
            case 3 -> SWT.COLOR_DARK_GREEN;
            case 4 -> SWT.COLOR_DARK_GRAY;
            case 5 -> SWT.COLOR_DARK_MAGENTA;
            case 6 -> SWT.COLOR_DARK_YELLOW;
            case 7 -> SWT.COLOR_DARK_CYAN;
            case 8 -> SWT.COLOR_RED;
            case 9 -> SWT.COLOR_BLUE;
            case 0 -> SWT.COLOR_BLACK;
            default -> SWT.COLOR_BLACK;
        };

    }
}
