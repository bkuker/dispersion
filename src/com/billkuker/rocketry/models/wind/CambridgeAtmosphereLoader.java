package com.billkuker.rocketry.models.wind;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.models.atmosphere.AtmosphericModel;
import net.sf.openrocket.models.wind.WindModel;
import net.sf.openrocket.util.Coordinate;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Loads atmospheres from Cambridge Simulator
 * 
 * @author bkuker
 *
 */
public class CambridgeAtmosphereLoader {

	double x[], y[], z[], a[], r[], t[];

	private final LayeredWindModel wm;
	private final AtmosphericModel am;

	/** Specific gas constant of dry air. */
	public static final double R = 287.053;

	public CambridgeAtmosphereLoader(final InputSource is) throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory spf = SAXParserFactory.newInstance();

		SAXParser saxParser = spf.newSAXParser();
		XMLReader xmlReader = saxParser.getXMLReader();
		xmlReader.setContentHandler(new DefaultHandler() {

			StringBuilder val;

			double[] split(final String s) {
				final String ss[] = s.split(",");
				final double ret[] = new double[ss.length];
				for (int i = 0; i < ss.length; i++) {
					ret[i] = Double.parseDouble(ss[i].trim());
				}
				return ret;
			}

			@Override
			public void startElement(String uri, String localName, String qName, Attributes atts) {
				val = new StringBuilder();
			}

			@Override
			public void characters(char[] ch, int start, int length) {
				val.append(new String(ch, start, length));
			}

			public void endElement(String uri, String localName, String qName) {
				if (qName.equals("Altitude"))
					a = split(val.toString());
				else if (qName.equals("XWind"))
					x = split(val.toString());
				else if (qName.equals("YWind"))
					y = split(val.toString());
				else if (qName.equals("ZWind"))
					z = split(val.toString());
				else if (qName.equals("Rho"))
					r = split(val.toString());
				else if (qName.equals("Theta"))
					t = split(val.toString());
			}
		});
		xmlReader.parse(is);

		wm = new LayeredWindModel();
		for (int i = 0; i < a.length; i++) {
			final Coordinate c = new Coordinate(x[i], y[i], z[i]);
			wm.addEntry(a[i], new VectorWindModel(c));
		}

		final UnivariateFunction rho = new LinearInterpolator().interpolate(a, r);
		final UnivariateFunction theta = new LinearInterpolator().interpolate(a, t);
		am = new AtmosphericModel() {

			@Override
			public int getModID() {
				return 0;
			}

			@Override
			public AtmosphericConditions getConditions(double altitude) {
				final double temperature = theta.value(altitude);
				final double density = rho.value(altitude);
				final double pressure = density * R * temperature;
				return new AtmosphericConditions(temperature, pressure);
			}
		};
	}

	public WindModel getWindModel() {
		return wm;
	}

	public AtmosphericModel getAtmosphericModel() {
		return am;
	}

	public static void main(String args[]) throws Exception {
		new CambridgeAtmosphereLoader(new InputSource(CambridgeAtmosphereLoader.class.getResourceAsStream("wpSWsterly02.xml")));
	}
}
