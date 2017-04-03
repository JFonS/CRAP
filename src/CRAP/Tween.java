package CRAP;

import interp.*;

public class Tween
{
	private Data data;
	private float keyTimeAbs;
	private float keyValue;
		
	public Tween(Data data, float keyTimeAbs, float keyValue)
	{
		assert data.isNumber();
		
		this.data       = data;
		this.keyTimeAbs = keyTimeAbs;
		this.keyValue   = keyValue;
	}
	
	public Data GetData() { return data; }
	public float GetKeyValue() { return keyValue; }
	public float GetKeyTimeAbs() { return keyTimeAbs; }
	
	public String toString() { return "<" + keyTimeAbs + ", " + keyValue + ">"; }
}
