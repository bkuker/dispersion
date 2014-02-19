package variables;

import java.util.Random;

public abstract class Variable extends Number {
	private static final long serialVersionUID = 1L;
	static final Random r = new Random(0);

	private String name;

	void setName(final String name) {
		if (this.name != null)
			throw new IllegalStateException("This " + getClass().getSimpleName() + " already assigned name " + name);
		this.name = name;
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
