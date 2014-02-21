package com.billkuker.rocketry.dispersion.vizualization;

import java.awt.geom.PathIterator;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUtessellator;
import javax.media.opengl.glu.GLUtessellatorCallback;
import javax.media.opengl.glu.GLUtessellatorCallbackAdapter;

import com.billkuker.rocketry.dispersion.analysys.Density.Path;

public class PathRenderer {

	private GLUtessellator tobj = GLU.gluNewTess();

	public synchronized void fill(final GLAutoDrawable drawable, Iterable<Path> paths) {
		for (Path p : paths) {
			fill(drawable, p);
		}
	}

	public synchronized void outline(final GLAutoDrawable drawable, Iterable<Path> paths) {
		for (Path p : paths) {
			outline(drawable, p);
		}
	}

	public synchronized void outline(final GLAutoDrawable drawable, Path path) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glBegin(GL.GL_LINE_LOOP);
		double d[] = new double[] { 0, 0, 0 };
		PathIterator pi = path.getPathIterator();
		while (!pi.isDone()) {
			pi.currentSegment(d);
			gl.glVertex3dv(d, 0);
			pi.next();
		}
		gl.glEnd();
	}

	public synchronized void fill(final GLAutoDrawable drawable, Path path) {
		final GL2 gl = drawable.getGL().getGL2();

		GLUtessellatorCallback cb = new GLUtessellatorCallbackAdapter() {
			@Override
			public void vertex(Object vertexData) {
				double d[] = (double[]) vertexData;
				gl.glVertex3dv(d, 0);
			}

			@Override
			public void begin(int type) {
				gl.glBegin(type);
			}

			@Override
			public void end() {
				gl.glEnd();
			}
		};

		GLU.gluTessCallback(tobj, GLU.GLU_TESS_VERTEX, cb);
		GLU.gluTessCallback(tobj, GLU.GLU_TESS_BEGIN, cb);
		GLU.gluTessCallback(tobj, GLU.GLU_TESS_END, cb);

		GLU.gluTessBeginPolygon(tobj, null);
		GLU.gluTessBeginContour(tobj);
		gl.glNormal3f(0, 0, 1);

		PathIterator pi = path.getPathIterator();

		while (!pi.isDone()) {
			double d[] = new double[] { 0, 0, 0 };
			pi.currentSegment(d);
			GLU.gluTessVertex(tobj, d, 0, d);
			pi.next();
		}

		GLU.gluTessEndContour(tobj);
		GLU.gluTessEndPolygon(tobj);
	}

}
