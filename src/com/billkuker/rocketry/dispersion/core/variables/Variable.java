package com.billkuker.rocketry.dispersion.core.variables;

import java.util.Map;
import java.util.Random;

public abstract class Variable extends Number {
	private static final long serialVersionUID = 1L;
	private static final Random r = new Random(0);

	private String name;

	public static void setName(final Number n, final String name) {
		if (n instanceof Variable) {
			((Variable) n).setName(name);
		}
	}
	
	private static ThreadLocal<Map<String, Double>> values = new ThreadLocal<Map<String,Double>>();
	
	public void setVariableValueMap(Map<String, Double> map){
		
	}
	
	protected double randomDouble(){
		return r.nextDouble();
	}
	
	protected double randomGaussian(){
		return r.nextDouble();
	}

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
