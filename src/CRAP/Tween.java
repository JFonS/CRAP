package CRAP;

import java.util.ArrayList;

import interp.*;

public class Tween
{
	private Data data;
	private float keyTimeAbs;
	private Data keyData;
	
	public Tween(Data data, float keyTimeAbs, Data keyData)
	{
		assert data.isNumber();
		
		this.data       = data;
		this.keyTimeAbs = keyTimeAbs;
		this.keyData   = keyData;
	}
	
	public Data GetData() { return data; }
	public Data GetKeyData() { return keyData; }
	public float GetKeyTimeAbs() { return keyTimeAbs; }
	
	public String toString() { return "<" + keyTimeAbs + ", " + keyData + ">"; }
}
