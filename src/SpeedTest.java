

import java.io.File;

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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class SpeedTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		LoggingSystemSetup.setupLoggingAppender();

		GuiModule guiModule = new GuiModule();
		Module pluginModule = new PluginModule();
		Injector injector = Guice.createInjector(guiModule, pluginModule);
		Application.setInjector(injector);
		guiModule.startLoader();

		((SwingPreferences) Application.getPreferences()).loadDefaultUnits();

		Databases.fakeMethod();

		GeneralRocketLoader grl = new GeneralRocketLoader(new File(
				"/Users/bkuker/git/openrocket/swing/resources/datafiles/examples/A simple model rocket.ork"));
		final OpenRocketDocument orig = grl.load();

		long start = -1;
		int count = 0;
		final int warmup = 200;
		System.out.println("Warming up...");
		for (int i = 0; i < 2000; i++) {
			OpenRocketDocument doc = orig.copy();
			Simulation s = doc.getSimulation(1).copy();
			s.getOptions().setRandomSeed(i);
			s.simulate();
			if (i == warmup) {
				System.out.println("Done Warmup");
				start = System.currentTimeMillis();
			}
			if (i > warmup) {
				count++;
				if (i % 10 == 0) {

					final long now = System.currentTimeMillis();
					final long time = now - start;
					final double seconds = time / 1000.0;
					final double simsPerSecond = count / seconds;
					System.out.println("Sims: " + count + "\tSims/s: " + simsPerSecond);
				}
			}
		}
	}

}
