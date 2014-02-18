package variables;


public class Gaussian extends Number {

	private static final long serialVersionUID = 1L;

	private final Number mean;
	private final Number stddev;

	public Gaussian(final Number mean, final Number stddev) {
		this.mean = mean;
		this.stddev = stddev;
	}

	public Gaussian(final Number stddev) {
		this.mean = 0;
		this.stddev = stddev;
	}

	@Override
	public double doubleValue() {
		return mean.doubleValue() + Rando.nextGaussian() * stddev.doubleValue();
	}

	@Override
	public float floatValue() {
		return (float) doubleValue();
	}

	@Override
	public int intValue() {
		return (int) doubleValue();
	}

	@Override
	public long longValue() {
		return (long) doubleValue();
	}

}
