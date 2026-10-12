package org.delafer.xanderView.gui.helpers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class UIHelpers {

	public static String asString(Object obj) {
		return obj != null ? obj.toString() : null;
	}

	public static void sleep(long ms) {

		try {
			Thread.sleep(ms);
		} catch (InterruptedException ignore) {
			Thread.currentThread().interrupt();
		}
	}

	  public static void disposeComposite(Composite composite,boolean disposeSelf) {
		    if(composite == null || composite.isDisposed())
		      return;
		  Control[] controls = composite.getChildren();
		  for(int i = 0 ; i < controls.length ; i++) {
		    Control control = controls[i];
		    if(control != null && ! control.isDisposed()) {
		      if(control instanceof Composite) {
		        disposeComposite((Composite) control,true);
		      }
		      try {
		        control.dispose();
		      } catch (SWTException e) {
		    	  e.printStackTrace();
		      }
		    }
		  }
		  // It's possible that the composite was destroyed by the child
		  if (!composite.isDisposed() && disposeSelf)
		    try {
		      composite.dispose();
		    } catch (SWTException e) {
		     e.printStackTrace();
		    }
		  }

		  public static void disposeComposite(Composite composite) {
		    disposeComposite(composite,true);
		  }


		  public static Image renderTransparency(Display display,Image background,Image foreground) {
			    //Checks
			    if( display == null || display.isDisposed() ||
			        background == null || background.isDisposed() ||
			        foreground == null || foreground.isDisposed()
			       ) return null;
			    Rectangle r1 = background.getBounds();
			    Rectangle r2 = foreground.getBounds();
			    if(! r1.equals(r2)) return null;

			    Image image = new Image(display,r1);
			    ImageData backData = background.getImageData();
			    ImageData foreData = foreground.getImageData();
			    ImageData imgData  = image.getImageData();

			    for(int y = 0 ; y < foreData.height ; y++) {
			      for(int x = 0 ; x < foreData.width ; x++) {
			       int cBack = backData.getPixel(x,y);
			       int cFore = foreData.getPixel(x,y);
			       int aFore = foreData.getAlpha(x,y);

			       int rBack =  cBack         & 0xFF;
			       int gBack = (cBack >> 8)  & 0xFF;
			       int bBack = (cBack >> 16) & 0xFF;

			       int rFore =  cFore         & 0xFF;
			       int gFore = (cFore >> 8)  & 0xFF;
			       int bFore = (cFore >> 16) & 0xFF;

			       int r = (rBack * aFore + (255 - aFore) * rFore) / 255;
			       r = r & 0xFF;
			       int g = (gBack * aFore + (255 - aFore) * gFore) / 255;
			       g = g & 0xFF;
			       int b = (bBack * aFore + (255 - aFore) * bFore) / 255;
			       b = b & 0xFF;

			       int color = r + (g << 8) + (b << 16);
			       imgData.setPixel(x,y,color);
			      }
			    }
			    return image;
			  }


		  public static FormData getFormData(Object left, Object right, Object top, Object bottom) {
			  FormData data = new FormData();
			  data.left = left instanceof Number ? new FormAttachment(((Number)left).intValue()) : left instanceof Control ? new FormAttachment(((Control)left)) : null;
			  data.right = right instanceof Number ? new FormAttachment(((Number)right).intValue()) : right instanceof Control ? new FormAttachment(((Control)right)) : null;
			  data.top = top instanceof Number ? new FormAttachment(((Number)top).intValue()) : top instanceof Control ? new FormAttachment(((Control)top)) : null;
			  data.bottom = bottom instanceof Number ? new FormAttachment(((Number)bottom).intValue()) : bottom instanceof Control ? new FormAttachment(((Control)bottom)) : null;
			  return data;
		  }

		  public static FormData setFormData(Control control, Object left, Object right, Object top, Object bottom) {
			  FormData data = getFormData(left, right, top, bottom);
			  control.setLayoutData(data);
			  return data;
		  }

		  public static MenuItem addMenuItem(Menu submenu, String text, int accelerator, Listener lst) {
				MenuItem item = new MenuItem (submenu, SWT.PUSH);
				item.setText (text);
				if (0 != accelerator)
					item.setAccelerator (accelerator);
				if (lst !=null)
				item.addListener (SWT.Selection, lst);
				return item;
			}

}
