package analysys;

import net.sf.openrocket.util.Coordinate;

public interface Density {
	public static double ONE_SIGMA = .68268949;
	public static double TWO_SIGMA = .954499736;
	public static double THREE_SIGMA = .997300204;

	public void add(Coordinate c);

	public Iterable<Path> getPaths(double mass);

	public static interface Path {
		public static final int SEG_CLOSE = 4;
		public static final int SEG_LINETO = 1;
		public static final int WIND_EVEN_ODD = 0;
		public static final int WIND_NON_ZERO = 1;

		public int currentSegment(double[] coords);

		public int currentSegment(float[] coords);

		public int getWindingRule();

		public boolean isDone();

		public void next();
	}
}
