package org.delafer.xanderView.scale.deprecated;

import java.awt.Image;
import java.awt.image.BufferedImage;

public class ClassicJava {


	public void resize() {
		BufferedImage sourceImage = null;
		int height = 800;
		Image scaled = sourceImage.getScaledInstance(-1, height, Image.SCALE_FAST);
		  BufferedImage bufferedScaled = new BufferedImage(scaled.getWidth(null),  scaled.getHeight(null), BufferedImage.TYPE_INT_RGB);
		  bufferedScaled.getGraphics().drawImage(scaled, 0, 0, null);
	}

}


/*

Graphics2D g2d = (Graphics2D) g;
// or BILINEAR for speed
g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

g2d.drawImage(img, x, y, width, height, null); // scale width, height

*/

/*
// elsewhere in the Canvas
buffer_ = createImage(bufW, bufH);

// In the render loop
Graphics2D g2 = (Graphics2D)g;
g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
g.drawImage(buffer_, 0, 0, getWidth(), getHeight(), null);
*/