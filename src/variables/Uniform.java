package variables;

import java.util.Random;

public class Uniform extends Number {
	private static final long serialVersionUID = 1L;
	private static final Random r = new Random(0);
	private final Number min;
	private final Number max;

	public Uniform(final Number min, final Number max) {
		this.min = min;
		this.max = max;
	}

	@Override
	public double doubleValue() {
		return r.nextDouble() * (max.doubleValue() - min.doubleValue()) + min.doubleValue();
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
