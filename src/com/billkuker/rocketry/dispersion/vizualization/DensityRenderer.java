package com.billkuker.rocketry.dispersion.vizualization;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import com.billkuker.rocketry.dispersion.analysys.Density;
import com.billkuker.rocketry.dispersion.analysys.Density.Path;

public class DensityRenderer extends PathRenderer {

	public synchronized void draw(final GLAutoDrawable drawable, Density d) {
		GL2 gl = drawable.getGL().getGL2();

		Iterable<Path> s1 = d.getPaths(Density.ONE_SIGMA);
		Iterable<Path> s2 = d.getPaths(Density.TWO_SIGMA);
		Iterable<Path> s3 = d.getPaths(Density.THREE_SIGMA);

		gl.glLineWidth(2);

		gl.glColor4d(1, 1, 0, .15);
		fill(drawable, s3);

		gl.glColor3d(1, 1, 0);
		outline(drawable, s3);

		gl.glColor4d(1, .5, 0, .15);
		fill(drawable, s2);

		gl.glColor3d(1, .5, 0);
		outline(drawable, s2);

		gl.glColor4d(1, 0, 0, .2);
		fill(drawable, s1);

		gl.glColor3d(1, 0, 0);
		outline(drawable, s1);

	}

}
