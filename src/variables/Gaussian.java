package variables;

public class Gaussian extends Variable {

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
		return mean.doubleValue() + randomGaussian() * stddev.doubleValue();
	}

}
