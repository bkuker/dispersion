import java.util.Collection;
import java.util.Vector;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import net.sf.openrocket.util.Coordinate;
import dr.geo.KernelDensityEstimator2D;
import dr.geo.contouring.ContourPath;

public class Density {

	private static Collection<Coordinate> points = new Vector<Coordinate>();
	private KernelDensityEstimator2D kde;

	ContourPath c90[] = null, c99[] = null, c9999[] = null;

	public synchronized void add(Coordinate c) {
		points.add(c);
		getKDE();
	}

	private synchronized void getKDE() {
		if (!points.isEmpty()) {
			double x[] = new double[points.size()];
			double y[] = new double[points.size()];
			int i = 0;
			for (Coordinate c : points) {
				x[i] = c.x;
				y[i] = c.y;
				i++;
			}
			kde = new KernelDensityEstimator2D(x, y, new double[] { 5, 5 }, 30, null);

			c90 = kde.getContourPaths(.90);
			c99 = kde.getContourPaths(.99);
			c9999 = kde.getContourPaths(.9999);
		}
	}

	public synchronized void draw(final GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glColor3d(1, 0, 0);
		draw(drawable, c90);
		gl.glColor3d(1, .5, 0);
		draw(drawable, c99);
		gl.glColor3d(1, 1, 0);
		draw(drawable, c9999);
	}

	public synchronized void draw(final GLAutoDrawable drawable, ContourPath[] paths) {
		if (paths == null)
			return;
		GL2 gl = drawable.getGL().getGL2();

		for (ContourPath cp : paths) {
			double x[] = cp.getAllX();
			double y[] = cp.getAllY();
			gl.glLineWidth(3);
			gl.glBegin(GL.GL_LINE_LOOP);
			for (int i = 0; i < x.length; i++) {
				gl.glVertex3d(x[i], y[i], .1);
			}
			gl.glEnd();
		}
	}

}
