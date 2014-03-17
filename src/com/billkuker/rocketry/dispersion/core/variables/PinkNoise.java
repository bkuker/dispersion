package com.billkuker.rocketry.dispersion.core.variables;

/**
 * A class that provides a source of pink noise with a power spectrum density
 * proportional to 1/f^alpha. The values are computed by applying an IIR filter
 * to generated Gaussian random numbers. The number of poles used in the filter
 * may be specified. Values as low as 3 produce good results, but using a larger
 * number of poles allows lower frequencies to be amplified. Below the cutoff
 * frequency the power spectrum density if constant.
 * <p>
 * The IIR filter use by this class is presented by N. Jeremy Kasdin,
 * Proceedings of the IEEE, Vol. 83, No. 5, May 1995, p. 822.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class PinkNoise extends Variable {
	private static final long serialVersionUID = 1L;
	private final int poles;
	private final double[] multipliers;

	private double[] values;

	/**
	 * Generate pink noise with alpha=1.0 using a five-pole IIR.
	 */
	public PinkNoise() {
		this(1.0, 5);
	}

	/**
	 * Generate a specific pink noise using a five-pole IIR.
	 * 
	 * @param alpha
	 *            the exponent of the pink noise, 1/f^alpha.
	 */
	public PinkNoise(double alpha) {
		this(alpha, 5);
	}

	/**
	 * Generate pink noise specifying alpha, the number of poles and the
	 * randomness source.
	 * 
	 * @param alpha
	 *            the exponent of the pink noise, 1/f^alpha.
	 * @param poles
	 *            the number of poles to use.
	 * @param random
	 *            the randomness source.
	 */
	public PinkNoise(double alpha, int poles) {

		this.poles = poles;
		this.multipliers = new double[poles];

		double a = 1;
		for (int i = 0; i < poles; i++) {
			a = (i - alpha / 2) * a / (i + 1);
			multipliers[i] = a;
		}

		// Fill the history with random values
		this.values = new double[poles];
		for (int i = 0; i < 5 * poles; i++)
			this.doubleValue();
	}

	@Override
	public double doubleValue() {
		double x = randomGaussian();

		for (int i = 0; i < poles; i++) {
			x -= multipliers[i] * values[i];
		}
		System.arraycopy(values, 0, values, 1, values.length - 1);
		values[0] = x;

		return x;
	}

}
