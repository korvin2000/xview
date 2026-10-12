package org.delafer.xanderView.gui.helpers;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Contains static methods for performing simple geometric operations
 * on the SWT geometry classes (the two methods used from the Eclipse JFace {@code Geometry}).
 *
 * @since 3.0
 */
public class Geometry {

    /**
     * Prevent this class from being instantiated.
     *
     * @since 3.0
     */
    private Geometry() {
    	//This is not instantiated
    }

    /**
     * Returns the square of the distance between two points.
     * <p>This is preferred over the real distance when searching
     * for the closest point, since it avoids square roots.</p>
     *
     * @param p1 first endpoint
     * @param p2 second endpoint
     * @return the square of the distance between the two points
     *
     * @since 3.0
     */
    public static int distanceSquared(Point p1, Point p2) {
        int term1 = p1.x - p2.x;
        int term2 = p1.y - p2.y;
        return term1 * term1 + term2 * term2;
    }

    /**
     * Returns the point in the center of the given rectangle.
     *
     * @param rect rectangle being computed
     * @return a Point at the center of the given rectangle.
     * @since 3.0
     */
    public static Point centerPoint(Rectangle rect) {
        return new Point(rect.x + rect.width / 2, rect.y + rect.height / 2);
    }
}
