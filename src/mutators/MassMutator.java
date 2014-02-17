package mutators;
import net.sf.openrocket.rocketcomponent.RocketComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MassMutator extends AbstractComponentMutator {
	private static final Logger log = LoggerFactory.getLogger(MassMutator.class);;
	private final Number pct;

	public MassMutator(final Number percent) {
		this.pct = percent;
	}

	protected void mutate(final RocketComponent c) {
		log.debug("Mutating " + c);
		if (c.isMassive()) {
			double oldMass = c.getMass();
			double d = pct.doubleValue() * oldMass;
			log.debug("Changing {} by {}", oldMass, d);
			c.setMassOverridden(true);
			c.setOverrideMass(oldMass + d);
		}
	}
}
