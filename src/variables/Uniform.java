package variables;

public class Uniform extends Number {
	private static final long serialVersionUID = 1L;
	private final Number min;
	private final Number max;

	public Uniform(final Number min, final Number max) {
		this.min = min;
		this.max = max;
	}

	@Override
	public double doubleValue() {
		return Rando.nextDouble() * (max.doubleValue() - min.doubleValue()) + min.doubleValue();
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
