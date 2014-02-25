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
			fill(drawable, p.getPathIterator());
		}
	}

	public synchronized void outline(final GLAutoDrawable drawable, Iterable<Path> paths) {
		for (Path p : paths) {
			outline(drawable, p.getPathIterator());
		}
	}

	public synchronized void outline(final GLAutoDrawable drawable, PathIterator pi) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glBegin(GL.GL_LINE_LOOP);
		double d[] = new double[] { 0, 0, 0 };
		
		while (!pi.isDone()) {
			int type = pi.currentSegment(d);
			if ( type == PathIterator.SEG_MOVETO){
				gl.glEnd();
				gl.glBegin(GL.GL_LINE_LOOP);
			}
			gl.glVertex3dv(d, 0);
			pi.next();
		}
		gl.glEnd();
	}

	public synchronized void fill(final GLAutoDrawable drawable, PathIterator pi) {
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
