package CRAP;

import java.util.ArrayList;

import interp.*;

public class Tween
{
	private Data data;
	private float keyTimeAbs;
	private Data keyData;
	private String interpType;
	
	public Tween(Data data, float keyTimeAbs, Data keyData, String interpType)
	{
		assert data.isNumber();
		
		this.data       = data;
		this.keyTimeAbs = keyTimeAbs;
		this.keyData   = keyData;
		this.interpType = interpType;
	}
	
	public Tween(Data data, float keyTimeAbs, Data keyData)
	{
		this(data,keyTimeAbs,keyData,"Linear");
	}
	
	public Data GetData() { return data; }
	public Data GetKeyData() { return keyData; }
	public float GetKeyTimeAbs() { return keyTimeAbs; }
	public String GetInterpType() { return interpType; }
	
	public String toString() { return "<" + keyTimeAbs + ", " + keyData + ">"; }
}
