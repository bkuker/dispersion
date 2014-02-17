import java.io.File;
import java.util.Random;

import javax.swing.JFrame;

import mutators.MassMutator;
import mutators.ParachuteFailure;
import mutators.RodAngleMutator;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
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

		// RandomSeed.setSeed(0);

		/*
		 * Setup the uncaught exception handler
		 * log.info("Registering exception handler"); SwingExceptionHandler
		 * exceptionHandler = new SwingExceptionHandler();
		 * Application.setExceptionHandler(exceptionHandler);
		 * exceptionHandler.registerExceptionHandler();
		 */

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

		JFrame f = new JFrame();
		f.setSize(1024, 768);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Display d = new Display();
		f.setContentPane(d);
		f.setVisible(true);

		GeneralRocketLoader grl = new GeneralRocketLoader(new File(
				"/Users/bkuker/git/openrocket/swing/resources/datafiles/examples/A simple model rocket.ork"));
		final OpenRocketDocument orig = grl.load();

		Random r = new Random(0);

		for (int i = 0; i < 10000; i++) {
			OpenRocketDocument doc = orig.copy();
			Simulation s = doc.getSimulation(1).copy();

			new MassMutator(new Gaussian(new Gaussian(0.0, 0.05), 0.05)).mutate(doc.getRocket());

			new ParachuteFailure(new Odds(.05)).mutate(doc.getRocket());
			
			new RodAngleMutator(new Gaussian(0.05), new Gaussian(0.1)).mutate(s.getOptions());

			s.getOptions().setRandomSeed(r.nextInt());
			s.simulate();

			d.addSimulation(s);

			log.info("Sim #{}", i);
			Thread.sleep(100);

		}

	}
}
