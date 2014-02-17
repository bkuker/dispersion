package variables;
import java.util.Random;

public class Odds {

	private static final Random r = new Random(0);
	private final double pct;

	public Odds(final double pct) {
		this.pct = pct;
	}

	public boolean occurs() {
		return r.nextDouble() <= pct;
	}
}
