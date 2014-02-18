package analysys;

import java.util.Collection;
import java.util.Vector;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import net.sf.openrocket.util.Coordinate;
import dr.geo.KernelDensityEstimator2D;
import dr.geo.contouring.ContourPath;

public class BeastKDEDensity implements Density {

	private static Collection<Coordinate> points = new Vector<Coordinate>();
	private KernelDensityEstimator2D kde;

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
			//TODO: How to choose bandwidth?
			kde = new KernelDensityEstimator2D(x, y, new double[] { 5, 5 }, 50, null);
		}
	}

	@Override
	public synchronized Iterable<Path> getPaths(double mass) {
		Vector<Path> ret = new Vector<Density.Path>();
		if (kde == null)
			return ret;

		ContourPath paths[] = kde.getContourPaths(mass);
		for (final ContourPath p : paths) {
			ret.add(new Path() {
				int i = 0;
				double x[] = p.getAllX();
				double y[] = p.getAllY();
				int len = x.length;

				@Override
				public void next() {
					i++;
				}

				@Override
				public boolean isDone() {
					return i == len;
				}

				@Override
				public int getWindingRule() {
					return WIND_EVEN_ODD;
				}

				@Override
				public int currentSegment(float[] coords) {
					coords[0] = (float) x[i];
					coords[1] = (float) y[i];
					return i == len ? SEG_CLOSE : SEG_LINETO;
				}

				@Override
				public int currentSegment(double[] coords) {
					coords[0] = x[i];
					coords[1] = y[i];
					return i == len ? SEG_CLOSE : SEG_LINETO;
				}
			});
		}

		return ret;
	}

	public synchronized void draw(final GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glColor3d(1, 0, 0);
		draw(drawable, getPaths(ONE_SIGMA));
		gl.glColor3d(1, .5, 0);
		draw(drawable, getPaths(TWO_SIGMA));
		gl.glColor3d(1, 1, 0);
		draw(drawable, getPaths(THREE_SIGMA));
	}

	public synchronized void draw(final GLAutoDrawable drawable, Iterable<Path> paths) {
		if (paths == null)
			return;
		GL2 gl = drawable.getGL().getGL2();

		for (Path p : paths) {
			gl.glLineWidth(3);
			gl.glBegin(GL.GL_LINE_LOOP);
			double d[] = new double[] { 0, 0, .1 };
			while (!p.isDone()) {
				p.currentSegment(d);
				gl.glVertex3dv(d, 0);
				p.next();
			}
			gl.glEnd();
		}

	}
}
