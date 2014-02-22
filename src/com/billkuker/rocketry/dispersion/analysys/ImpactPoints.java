package com.billkuker.rocketry.dispersion.analysys;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.util.Coordinate;

import com.billkuker.rocketry.dispersion.core.Engine.SampleListener;
import com.billkuker.rocketry.dispersion.core.Sample;

public class ImpactPoints {

	public Coordinate[] impactCoordiantes(Sample sample) {
		Simulation s = sample.getSimulation();
		Coordinate[] ret = new Coordinate[s.getSimulatedData().getBranchCount()];
		for (int bi = 0; bi < s.getSimulatedData().getBranchCount(); bi++) {
			FlightDataBranch b = s.getSimulatedData().getBranch(bi);

			ret[bi] = new Coordinate(b.getLast(FlightDataType.TYPE_POSITION_X),
					b.getLast(FlightDataType.TYPE_POSITION_Y), b.getLast(FlightDataType.TYPE_VELOCITY_Z));

		}
		return ret;
	}

}
