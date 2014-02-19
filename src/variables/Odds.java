package variables;

public class Odds {

	private final double pct;

	public Odds(final double pct) {
		this.pct = pct;
	}

	public boolean occurs() {
		return Variable.r.nextDouble() <= pct;
	}
}
