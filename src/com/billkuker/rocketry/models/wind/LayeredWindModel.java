package com.billkuker.rocketry.models.wind;

import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.openrocket.models.wind.WindModel;
import net.sf.openrocket.util.Coordinate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wind model consisting of multiple wind models at different altitudes. The
 * returned wind will be a linear interpolation between the two nearest
 * altitudes for which wind models are set, or the raw output from the single
 * nearest wind model if the altitude specified is above the highest or below
 * the lowest
 * 
 * @author bkuker
 * 
 */
public class LayeredWindModel implements WindModel {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(LayeredWindModel.class);

	private SortedMap<Double, WindModel> models = new TreeMap<Double, WindModel>();

	private int modID = 0;

	/**
	 * Add a wind model
	 * 
	 * @param altitude
	 *            The altitude for this model
	 * @param model
	 *            The model
	 */
	public void addEntry(final double altitude, final WindModel model) {
		modID++;
		models.put(altitude, model);
	}

	@Override
	public int getModID() {
		int ret = modID;
		for (WindModel m : models.values())
			ret += m.getModID();
		return ret;
	}

	@Override
	public Coordinate getWindVelocity(final double time, final double altitude) {
		double low = 0;
		double high = 0;
		WindModel lowModel = null;
		WindModel highModel = null;

		try {
			low = models.headMap(altitude).lastKey();
			lowModel = models.get(low);
		} catch (NoSuchElementException e) {
			//Left blank
		}
		try {
			high = models.tailMap(altitude).firstKey();
			highModel = models.get(high);
		} catch (NoSuchElementException e) {

		}

		if (lowModel == null) {
			lowModel = highModel;
			low = high;
		}

		if (highModel == null) {
			highModel = lowModel;
			high = low;
		}

		if (lowModel == null || highModel == null)
			throw new Error("No wind model");

		Coordinate lowVal = lowModel.getWindVelocity(time, altitude);
		if (lowModel == highModel)
			return lowVal;
		Coordinate highVal = highModel.getWindVelocity(time, altitude);

		double a = altitude - low;
		double h = high - low;
		double highFrac = a / h;
		double lowFrac = 1 - highFrac;

		return lowVal.multiply(lowFrac).add(highVal).multiply(highFrac);

	}

}
