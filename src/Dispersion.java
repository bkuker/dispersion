import java.io.File;
import java.util.Vector;

import javax.swing.JFrame;

import mutators.MassMutator;
import mutators.Mutator;
import mutators.ParachuteFailure;
import mutators.RodAngleMutator;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.GeneralRocketLoader;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.logging.LoggingSystemSetup;
import net.sf.openrocket.plugin.PluginModule;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.GuiModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import variables.Gaussian;
import variables.Odds;
import variables.Uniform;
import visualization.Display;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class Dispersion {

	private static final Logger log = LoggerFactory.getLogger(Dispersion.class);

	public static void main(String args[]) throws Exception {
		Dispersion d = new Dispersion();
		d.startup();
	}

	private void startup() throws Exception {

		LoggingSystemSetup.setupLoggingAppender();
		LoggingSystemSetup.addConsoleAppender();

		// Load motors etc.
		log.info("Loading databases");

		GuiModule guiModule = new GuiModule();
		Module pluginModule = new PluginModule();
		Injector injector = Guice.createInjector(guiModule, pluginModule);
		Application.setInjector(injector);

		guiModule.startLoader();

		// Set the best available look-and-feel
		log.info("Setting best LAF");
		GUIUtil.setBestLAF();

		// Load defaults
		((SwingPreferences) Application.getPreferences()).loadDefaultUnits();

		Databases.fakeMethod();
		
		
		//Set up GUI

		JFrame f = new JFrame();
		f.setSize(1024, 768);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final Display d = new Display();
		f.setContentPane(d);
		f.setVisible(true);

		//Load a model
		GeneralRocketLoader grl = new GeneralRocketLoader(new File(
				"/Users/bkuker/git/openrocket/swing/resources/datafiles/examples/A simple model rocket.ork"));
		final OpenRocketDocument orig = grl.load();

		
		//Set up Mutators
		Vector<Mutator> m = new Vector<Mutator>();
		m.add(new MassMutator(new Gaussian(new Gaussian(0.0, 0.05), 0.05)));
		m.add(new ParachuteFailure(new Odds(.1)));
		m.add(new RodAngleMutator(new Gaussian(0.04), new Uniform(-Math.PI, Math.PI)));

		//Run it!
		Engine e = new Engine(orig, 1, m);
		e.addSimListener(new Engine.SimListener() {
			@Override
			public void simComplete(Engine.Sim s) {
				d.addSimulation(s.getSimulation());
			}
		});
		e.run(10);
	}
}
