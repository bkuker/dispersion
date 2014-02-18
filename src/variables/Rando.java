package variables;

import java.util.Random;

class Rando {
	private static final Random r = new Random(0);

	static final double nextDouble() {
		return r.nextDouble();
	}

	static final double nextGaussian() {
		return r.nextGaussian();
	}
}
