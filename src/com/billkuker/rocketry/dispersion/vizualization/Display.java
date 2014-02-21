package com.billkuker.rocketry.dispersion.vizualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.media.opengl.DebugGL2;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.MouseInputAdapter;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.plot.EventGraphics;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.billkuker.rocketry.dispersion.analysys.BeastKDEDensity;

public class Display extends JPanel implements GLEventListener {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(Display.class);

	static {
		// this allows the GL canvas and things like the motor selection
		// drop down to z-order themselves.
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	}

	static Method color;
	static {
		try {
			color = EventGraphics.class.getDeclaredMethod("getEventColor",
					FlightEvent.Type.class);
		} catch (Exception e) {
			throw new Error(e);
		}
		color.setAccessible(true);
	}

	static Color getEventColor(FlightEvent.Type t) {
		try {
			return (Color) color.invoke(null, t);
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	private Component canvas;
	private double ratio;

	private double viewAlt = 45;
	private double viewAz = 0;
	private double viewDist = 100;

	private final int LIMIT = 100;
	private final Queue<Simulation> simulations = new ConcurrentLinkedQueue<Simulation>();
	Simulation highlighted;

	private final Queue<Coordinate> landing = new ConcurrentLinkedQueue<Coordinate>();
	private final Queue<Coordinate> impact = new ConcurrentLinkedQueue<Coordinate>();

	GL2 gl;
	GLU glu;
	GLUquadric q;

	private final BeastKDEDensity d = new BeastKDEDensity();

	public void addSimulation(final Simulation s) {
		simulations.offer(s);
		// highlighted = s;

		if (simulations.size() > LIMIT)
			simulations.poll();

		for (int bi = 0; bi < s.getSimulatedData().getBranchCount(); bi++) {
			FlightDataBranch b = s.getSimulatedData().getBranch(bi);

			final Coordinate c = new Coordinate(
					b.getLast(FlightDataType.TYPE_POSITION_X),
					b.getLast(FlightDataType.TYPE_POSITION_Y), 0);
			final double vz = b.getLast(FlightDataType.TYPE_VELOCITY_Z);

			if (vz < -10)
				impact.offer(c);
			else
				landing.offer(c);
			d.add(c);
		}

		repaint();
	}

	public Display() {
		this.setLayout(new BorderLayout());
		initGLCanvas();
		setupMouseListeners();
	}

	private void initGLCanvas() {
		try {
			log.debug("Setting up GL capabilities...");
			final GLProfile glp = GLProfile.get(GLProfile.GL2);

			final GLCapabilities caps = new GLCapabilities(glp);

			// TODO prefs
			caps.setSampleBuffers(true);
			caps.setNumSamples(6);
			canvas = new GLCanvas(caps);

			((GLAutoDrawable) canvas).addGLEventListener(this);
			this.add(canvas, BorderLayout.CENTER);
		} catch (Throwable t) {
			log.error("An error occurred creating 3d View", t);
			canvas = null;
			this.add(new JLabel("Unable to load 3d Libraries: "
					+ t.getMessage()));
		}
	}

	private void setupMouseListeners() {
		MouseInputAdapter a = new MouseInputAdapter() {
			int lastX;
			int lastY;
			MouseEvent pressEvent;

			@Override
			public void mousePressed(final MouseEvent e) {
				lastX = e.getX();
				lastY = e.getY();
				pressEvent = e;
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				final int clicks = Math.abs(e.getWheelRotation());
				final boolean pos = clicks == e.getWheelRotation();
				for (int i = 0; i < clicks; i++) {

					viewDist *= pos ? 1.1 : .90;

				}
				// viewDist += 7 * e.getWheelRotation();
				// viewDist = MathUtil.clamp(viewDist, 7, 500);
				viewDist = Math.max(viewDist, 15);
				Display.this.repaint();
			}

			@Override
			public void mouseDragged(final MouseEvent e) {
				// You can get a drag without a press while a modal dialog is
				// shown
				if (pressEvent == null)
					return;

				final double height = canvas.getHeight();
				final double width = canvas.getWidth();
				final double x1 = (width - 2 * lastX) / width;
				final double y1 = (2 * lastY - height) / height;
				final double x2 = (width - 2 * e.getX()) / width;
				final double y2 = (2 * e.getY() - height) / height;

				viewAlt -= (y1 - y2) * 100;
				viewAz += (x1 - x2) * 100;

				viewAlt = MathUtil.clamp(viewAlt, 1, 90);

				lastX = e.getX();
				lastY = e.getY();

				Display.this.repaint();
			}
		};

		canvas.addMouseWheelListener(a);
		canvas.addMouseMotionListener(a);
		canvas.addMouseListener(a);
	}

	@Override
	public void paintImmediately(Rectangle r) {
		super.paintImmediately(r);
		if (canvas != null)
			((GLAutoDrawable) canvas).display();
	}

	@Override
	public void paintImmediately(int x, int y, int w, int h) {
		super.paintImmediately(x, y, w, h);
		if (canvas != null)
			((GLAutoDrawable) canvas).display();
	}

	@Override
	public void display(final GLAutoDrawable drawable) {
		gl = drawable.getGL().getGL2();

		if (glu == null) {
			glu = new GLU();
			q = glu.gluNewQuadric();
		}

		gl.glEnable(GL.GL_MULTISAMPLE);

		gl.glClearDepth(1.0f); // clear z-buffer to the farthest
		gl.glDepthFunc(GL.GL_LESS);
		gl.glEnable(GL.GL_DEPTH_TEST);

		gl.glClearColor(.90f, .90f, 1, 1);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(60, ratio, viewDist / 10, viewDist * 10);
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);

		gl.glLoadIdentity();

		glu.gluLookAt(0, 0, viewDist, 0, 0, 0, 0, 1, 0);

		gl.glRotated(-90, 1, 0, 0);

		gl.glRotated(viewAlt, 1, 0, 0);
		gl.glRotated(viewAz, 0, 0, 1);

		gl.glTranslated(0, 0, -viewDist / 3);

		gl.glFogi(GL2.GL_FOG_MODE, GL2.GL_LINEAR);
		gl.glFogfv(GL2.GL_FOG_COLOR, new float[] { .9f, .95f, .85f }, 0);
		gl.glFogf(GL2.GL_FOG_DENSITY, 0.1f);
		gl.glHint(GL2.GL_FOG_HINT, GL2.GL_NICEST);
		gl.glFogf(GL2.GL_FOG_START, (float) (viewDist * 5));
		gl.glFogf(GL2.GL_FOG_END, (float) (viewDist * 9));
		gl.glEnable(GL2.GL_FOG);

		drawGroundGrid(drawable);
		gl.glDisable(GL.GL_DEPTH_TEST);
		new DensityRenderer().draw(drawable, d);
		gl.glEnable(GL.GL_DEPTH_TEST);

		gl.glDisable(GL2.GL_FOG);

		drawUpArrow(drawable);

		drawPoints(drawable);

		if (viewAlt < 89)
			drawSimulations(drawable);

		gl = null;
	}

	public void drawSimulations(final GLAutoDrawable drawable) {
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		// gl.glDepthMask(false);
		double a = 1 - (.01 * simulations.size());
		for (final Simulation s : simulations) {
			// a = a + .01;
			a = 1;
			drawSimulation(s, s == highlighted, a, drawable);
		}
		// gl.glDepthMask(true);
	}

	public void drawSimulation(final Simulation s, final boolean highlight,
			final double a, final GLAutoDrawable drawable) {

		for (int b = 0; b < s.getSimulatedData().getBranchCount(); b++) {
			drawBranch(s.getSimulatedData().getBranch(b), false, a, drawable);
		}

	}

	public void drawBranch(final FlightDataBranch b, final boolean highlight,
			final double a, final GLAutoDrawable drawable) {
		int n = b.getLength();
		List<Double> t = b.get(FlightDataType.TYPE_TIME);
		List<Double> x = b.get(FlightDataType.TYPE_POSITION_X);
		List<Double> y = b.get(FlightDataType.TYPE_POSITION_Y);
		List<Double> z = b.get(FlightDataType.TYPE_ALTITUDE);
		List<Double> vz = b.get(FlightDataType.TYPE_VELOCITY_Z);

		List<Double> thrust = b.get(FlightDataType.TYPE_THRUST_FORCE);
		double maxThrust = b.getMaximum(FlightDataType.TYPE_THRUST_FORCE);

		/*
		 * if (simulations.size() < 10) gl.glLineWidth(3); else if
		 * (simulations.size() < 100) gl.glLineWidth(2); else gl.glLineWidth(1);
		 */
		gl.glLineWidth(1);

		gl.glBegin(GL.GL_LINE_STRIP);
		for (int i = 0; i < n; i++) {
			double v = vz.get(i);
			if (v < -5) {
				v = MathUtil.clamp(v, -20, -5);
				v = -v - 5;
				v = v / 15;
				gl.glColor4d(v, 0, 0, a);
			} else {
				double f = thrust.get(i);
				if (f > 0) {
					double c = f / maxThrust + .3;
					gl.glColor4d(c, .8 * c, 0, a);
				} else {
					gl.glColor4d(0, 0, 0, a);
				}
			}
			gl.glVertex3d(x.get(i), y.get(i), z.get(i));
		}
		gl.glEnd();

		if (highlight) {
			gl.glLineWidth(1);
			gl.glColor4d(0, 0, 0, .15);
			gl.glBegin(GL.GL_TRIANGLE_STRIP);
			for (int i = 0; i < n; i++) {
				gl.glVertex3d(x.get(i), y.get(i), z.get(i));
				gl.glVertex3d(x.get(i), y.get(i), 0);
			}
			gl.glEnd();
			gl.glBegin(GL.GL_LINES);
			for (int i = 0; i < n; i++) {
				if (i % 20 == 0) {
					gl.glVertex3d(x.get(i), y.get(i), z.get(i));
					gl.glVertex3d(x.get(i), y.get(i), 0);
				}
			}
			gl.glEnd();
		}

		if (false) {
			List<FlightEvent> events = b.getEvents();
			Collections.sort(events);
			outer: for (int i = 0; i < n - 1; i++) {
				while (events.get(0).getTime() < t.get(i)) {
					FlightEvent e = events.remove(0);

					Color color = getEventColor(e.getType());
					gl.glColor4d(color.getRed() / 255.0,
							color.getGreen() / 255.0, color.getBlue() / 255.0,
							1);

					gl.glPushMatrix();
					gl.glTranslated(x.get(i), y.get(i), z.get(i));

					glu.gluSphere(q, .006 * viewDist, 10, 10);
					gl.glPopMatrix();
					if (events.size() == 0)
						break outer;
				}
			}
		}
	}

	public void drawPoints(final GLAutoDrawable drawable) {

		gl.glColor3d(1, 0, 0);

		for (final Coordinate c : impact) {
			gl.glPushMatrix();
			gl.glTranslated(c.x, c.y, c.z);
			glu.gluSphere(q, .007 * viewDist, 8, 5);
			gl.glPopMatrix();
		}

		gl.glColor3d(.1, .3, .1);
		for (final Coordinate c : landing) {
			gl.glPushMatrix();
			gl.glTranslated(c.x, c.y, c.z);
			glu.gluSphere(q, .007 * viewDist, 8, 5);
			gl.glPopMatrix();
		}
	}

	/**
	 * Draw a nice little 1m tall arrow at the origin, pointing up
	 * 
	 * @param drawable
	 */
	public void drawUpArrow(final GLAutoDrawable drawable) {

		gl.glColor3d(0.1, 0.5, 0.1);

		glu.gluSphere(q, 0.1f, 10, 10);
		glu.gluCylinder(q, .05, .05, .7, 10, 10);
		gl.glTranslated(0, 0, .7);
		glu.gluCylinder(q, .15, 0, .3, 20, 1);
		gl.glTranslated(0, 0, -.7);
	}

	public void drawGroundGrid(final GLAutoDrawable drawable) {

		// Draw a Big green square for the ground plane
		final int MAX = 10000;
		gl.glColor3d(.9, .95, .85);
		gl.glBegin(GL.GL_TRIANGLE_FAN);
		gl.glVertex3f(-MAX, -MAX, 0);
		gl.glVertex3f(-MAX, MAX, 0);
		gl.glVertex3f(MAX, MAX, 0);
		gl.glVertex3f(MAX, -MAX, 0);
		gl.glEnd();

		// Disable depth test, all of this gets drawn over ground plane
		gl.glDisable(GL.GL_DEPTH_TEST);

		// Draw grid
		final int LEVELS = 2;

		for (int level = LEVELS; level >= 0; level--) {
			int d = (int) (Math.pow(10, level));

			gl.glColor3d(1, 1, 1);
			gl.glBegin(GL.GL_TRIANGLE_FAN);
			int D = 50 * d;
			gl.glVertex3f(-D, -D, 0);
			gl.glVertex3f(-D, D, 0);
			gl.glVertex3f(D, D, 0);
			gl.glVertex3f(D, -D, 0);
			gl.glEnd();

			gl.glColor3d(.7, .75, .7);
			gl.glLineWidth(1);
			gl.glBegin(GL.GL_LINES);
			for (int x = -50 * d; x <= 50 * d; x += d) {
				gl.glVertex3f(x, -d * 50, 0);
				gl.glVertex3f(x, d * 50, 0);
				gl.glVertex3f(-d * 50, x, 0);
				gl.glVertex3f(d * 50, x, 0);
			}
			gl.glEnd();
			gl.glColor3d(0, 0, 0);
			gl.glLineWidth(2);
			gl.glBegin(GL.GL_LINES);
			d = d * 10;
			for (int x = -5 * d; x <= 5 * d; x += d) {
				gl.glVertex3f(x, -d * 5, 0);
				gl.glVertex3f(x, d * 5, 0);
				gl.glVertex3f(-d * 5, x, 0);
				gl.glVertex3f(d * 5, x, 0);
			}
			gl.glEnd();
		}
		gl.glEnable(GL.GL_DEPTH_TEST);
	}

	@Override
	public void dispose(final GLAutoDrawable drawable) {
		log.trace("GL - dispose() called");

	}

	@Override
	public void init(final GLAutoDrawable drawable) {
		log.trace("GL - init()");
		drawable.setGL(new DebugGL2(drawable.getGL().getGL2()));

		final GL2 gl = drawable.getGL().getGL2();

		gl.glClearDepth(1.0f); // clear z-buffer to the farthest
		gl.glDepthFunc(GL.GL_LESS); // the type of depth test to do
	}

	@Override
	public void reshape(final GLAutoDrawable drawable, final int x,
			final int y, final int w, final int h) {
		log.trace("GL - reshape()");
		ratio = (double) w / (double) h;
	}

	@SuppressWarnings("unused")
	private static class Bounds {
		double xMin, xMax, xSize;
		double yMin, yMax, ySize;
		double zMin, zMax, zSize;
		double rMax;
	}

	public static void main(String args[]) throws Exception {
		JFrame f = new JFrame();
		f.setSize(640, 480);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Display d = new Display();
		f.setContentPane(d);

		f.setVisible(true);
	}
}
