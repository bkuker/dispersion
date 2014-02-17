import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Collection;
import java.util.List;
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
import net.sf.openrocket.gui.figure3d.photo.exhaust.FlameRenderer;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Display extends JPanel implements GLEventListener {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(Display.class);

	static {
		// this allows the GL canvas and things like the motor selection
		// drop down to z-order themselves.
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	}

	private Component canvas;
	private double ratio;

	private double viewAlt = 45;
	private double viewAz = 0;
	private double viewDist = 10;

	private final Collection<Simulation> simulations = new ConcurrentLinkedQueue<Simulation>();

	public void addSimulation(final Simulation s) {
		simulations.add(s);
		repaint();
	}

	Display() {
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
			this.add(new JLabel("Unable to load 3d Libraries: " + t.getMessage()));
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
				viewDist += 1 * e.getWheelRotation();
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
		log.debug("display()");
		GL2 gl = drawable.getGL().getGL2();
		GLU glu = new GLU();
		GLUquadric q = glu.gluNewQuadric();

		gl.glEnable(GL.GL_MULTISAMPLE);

		gl.glClearDepth(1.0f); // clear z-buffer to the farthest
		gl.glDepthFunc(GL.GL_LESS);
		gl.glEnable(GL.GL_DEPTH_TEST);

		gl.glClearColor(1, 1, 1, 1);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(60, ratio, 2f, 300f);
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);

		gl.glLoadIdentity();

		glu.gluLookAt(0, 0, viewDist, 0, 0, 0, 0, 1, 0);

		gl.glRotated(-90, 1, 0, 0);

		gl.glRotated(viewAlt, 1, 0, 0);
		gl.glRotated(viewAz, 0, 0, 1);

		gl.glTranslated(0, 0, -viewDist/4);

		gl.glColor3d(0, 0, 0);
		glu.gluSphere(q, 0.1f, 10, 10);

		drawUpArrow(drawable);
		drawGroundGrid(drawable);
		drawSimulations(drawable);
	}

	public void drawSimulations(final GLAutoDrawable drawable) {
		for (final Simulation s : simulations) {
			drawSimulation(s, drawable);
		}
	}

	public void drawSimulation(final Simulation s, final GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		FlightDataBranch b = s.getSimulatedData().getBranch(0);
		int n = b.getLength();
		List<Double> x = b.get(FlightDataType.TYPE_POSITION_X);
		List<Double> y = b.get(FlightDataType.TYPE_POSITION_Y);
		List<Double> z = b.get(FlightDataType.TYPE_ALTITUDE);
		List<Double> vz = b.get(FlightDataType.TYPE_VELOCITY_Z);

		
		gl.glLineWidth(1);

		gl.glBegin(GL.GL_LINE_STRIP);
		for (int i = 0; i < n; i++) {
			gl.glColor3d(-vz.get(i) * .05, vz.get(i) * .01, vz.get(i) * .01);
			gl.glVertex3d(x.get(i), y.get(i), z.get(i));
		}
		gl.glEnd();
		
		if ( vz.get(n-1) < -10 ){
			gl.glColor3d(1,0,0);
			
			GLU glu = new GLU();
			GLUquadric q = glu.gluNewQuadric();
			gl.glPushMatrix();
			gl.glTranslated(x.get(n-1), y.get(n-1), 0);
			//gl.glScaled(1, 1, .5);
			glu.gluSphere(q, .5, 10, 10);
			gl.glPopMatrix();
			
		}
	}

	public void drawUpArrow(final GLAutoDrawable drawable) {

		GL2 gl = drawable.getGL().getGL2();

		gl.glColor3d(.7, 0, 0);
		gl.glLineWidth(2);

		gl.glBegin(GL.GL_LINES);
		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(0, 0, 1);
		gl.glEnd();

		GLU glu = new GLU();
		GLUquadric q = glu.gluNewQuadric();
		gl.glTranslated(0, 0, 1);
		glu.gluSphere(q, 0.1f, 10, 10);
		gl.glTranslated(0, 0, -1);
	}

	public void drawGroundGrid(final GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glColor3d(0, 0, 0);

		final int MAX = 100;

		for (int x = -MAX; x <= MAX; x++) {
			if (x % 10 == 0) {
				gl.glColor3d(0, 0, 0);
				gl.glLineWidth(2);
			} else {
				gl.glColor3d(.7, .75, .7);
				gl.glLineWidth(1);
			}
			gl.glBegin(GL.GL_LINES);
			gl.glVertex3f(x, -MAX, 0);
			gl.glVertex3f(x, MAX, 0);
			gl.glEnd();
		}

		for (int y = -MAX; y <= MAX; y++) {
			if (y % 10 == 0) {
				gl.glColor3d(0, 0, 0);
				gl.glLineWidth(2);
			} else {
				gl.glColor3d(.7, .75, .7);
				gl.glLineWidth(1);
			}
			gl.glBegin(GL.GL_LINES);
			gl.glVertex3f(-MAX, y, 0);
			gl.glVertex3f(MAX, y, 0);
			gl.glEnd();
		}

		gl.glBegin(GL.GL_LINES);
		gl.glVertex3f(0, -1, 0);
		gl.glVertex3f(0, 1, 0);
		gl.glEnd();
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

		FlameRenderer.init(gl);

	}

	@Override
	public void reshape(final GLAutoDrawable drawable, final int x, final int y, final int w, final int h) {
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
