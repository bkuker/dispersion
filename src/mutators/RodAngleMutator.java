package mutators;

import net.sf.openrocket.simulation.SimulationOptions;

public class RodAngleMutator implements SimulationOptionsMutator {

	private final Number dAngle;
	private final Number dDirection;
	public RodAngleMutator(Number dAngle, Number dDirection) {
		super();
		this.dAngle = dAngle;
		this.dDirection = dDirection;
	}
	@Override
	public void mutate(SimulationOptions op) {
		op.setLaunchRodAngle(op.getLaunchRodAngle() + dAngle.doubleValue());
		op.setLaunchRodDirection(op.getLaunchRodDirection() + dDirection.doubleValue());
	}

}
