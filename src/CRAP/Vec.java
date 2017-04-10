package CRAP;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Vec {
	private int size;
	private float[] values;
	
	public static final Map<String, Integer> SWIZZLER;

	static {
	    Map<String, Integer> map = new HashMap<String, Integer>();
	    map.put("x", 0);
	    map.put("y", 1);
	    map.put("z", 2);
	    map.put("w", 3);
	    
	    map.put("r", 0);
	    map.put("g", 1);
	    map.put("b", 2);
	    map.put("a", 3);
	    
	    SWIZZLER = Collections.unmodifiableMap(map);
	}
	
	public Vec(int s) 
	{ 
		this(s,0.0f);
	}
	
	public Vec(int s, float... v) 
	{ 
		assert s > 0 && s <= 4;
		size = s;
		values = new float[s];
		
		if (v.length == 1) 
		{
			for (int i = 0; i < s ; ++i) values[i] = v[0]; 
		}
		else
		{
			assert v.length >= s : "Wrong number of vector arguments.";
			for (int i = 0; i < s ; ++i) values[i] = v[i]; 
		}
	}
	
	public Vec(Vec v) 
	{ 
		size = v.size;
		values = v.values;
	}
	
	public int GetSize() { return size; }
	
	public float Get(int i) 
	{
		assert i < size : "Vector index too high.";
		return values[i];
	}
	
	public float Get(String i) 
	{
		assert i.length() == 1 : "Get only returns one element.";
		return values[SWIZZLER.get(i)];
	}
	
	public void Set(int i, float f) 
	{
		assert i < size : "Vector index too high.";
		values[i] = f;
	}
	
	public void Set(String elem, float f) 
	{
		assert elem.length() == 1 : "Swizzling not allowed in assignations.";
		values[SWIZZLER.get(elem)] = f;
	}
	
	public Vec Swizzle(String swizzle) 
	{
		int s = swizzle.length();
		assert s <= 4 : "Swizzling too long.";
		float[] v = new float[s];
		for (int i = 0; i < s; ++i) 
		{			
			v[i] = Get(Character.toString(swizzle.charAt(i)));
		}
		return new Vec(s,v);
	}
	
	public String toString() 
	{
		String str = "(";
		for (int i = 0; i < size; ++i)  str += values[i] + ", ";
		str += ")";
		str = str.replace(", )",")");
		return str;
	}
	
	////// Vec3 and Vec3 //////
	public boolean Equals(Vec v)
	{
		if (size != v.size) return false;
		
		for (int i = 0; i < size; ++i) 
		{
			if (values[i] != v.values[i]) return false;
		}
		return true;
	}
	
	public static Vec Sum(Vec l, Vec r)
	{
		assert l.size == r.size : "Can't add two vetors with different lengths.";
		int s = l.size;
		float[] newValues = new float[s];
		for (int i = 0; i < s; ++i) newValues [i] = l.values[i] + r.values[i];
		return new Vec(s, newValues);
	}
	
	public static Vec Sub(Vec l, Vec r)
	{
		assert l.size == r.size : "Can't subtract two vetors with different lengths.";
		int s = l.size;
		float[] newValues = new float[s];
		for (int i = 0; i < s; ++i) newValues [i] = l.values[i] - r.values[i];
		return new Vec(s, newValues);
	}
	
	public static Vec Mul(Vec l, Vec r)
	{
		assert l.size == r.size : "Can't multiply two vetors with different lengths.";
		int s = l.size;
		float[] newValues = new float[s];
		for (int i = 0; i < s; ++i) newValues [i] = l.values[i] * r.values[i];
		return new Vec(s, newValues);
	}
	
	public static Vec Div(Vec l, Vec r)
	{
		assert l.size == r.size : "Can't divide two vetors with different lengths.";
		int s = l.size;
		float[] newValues = new float[s];
		for (int i = 0; i < s; ++i) newValues [i] = l.values[i] / r.values[i];
		return new Vec(s, newValues);
	}
	
	public static Vec Mod(Vec l, Vec r) 
	{
		assert l.size == r.size : "Can't modulo two vetors with different lengths.";
		int s = l.size;
		float[] newValues = new float[s];
		for (int i = 0; i < s; ++i) newValues [i] = l.values[i] % r.values[i];
		return new Vec(s, newValues);
	}
	
	////// float and Vec3 //////
	public static Vec Sum(float l, Vec r) { return Vec.Sum(new Vec(r.size,l), r); }
	public static Vec Sub(float l, Vec r) { return Vec.Sub(new Vec(r.size,l), r); }
	public static Vec Mul(float l, Vec r) { return Vec.Mul(new Vec(r.size,l), r); }
	public static Vec Div(float l, Vec r) { return Vec.Div(new Vec(r.size,l), r); }
	public static Vec Mod(float l, Vec r) { return Vec.Mod(new Vec(r.size,l), r); }
	
	////// Vec3 and float //////
	public static Vec Sum(Vec l, float r) { return Vec.Sum(l, new Vec(l.size, r)); }
	public static Vec Sub(Vec l, float r) { return Vec.Sub(l, new Vec(l.size, r)); }
	public static Vec Mul(Vec l, float r) { return Vec.Mul(l, new Vec(l.size, r)); }
	public static Vec Div(Vec l, float r) { return Vec.Div(l, new Vec(l.size, r)); }
	public static Vec Mod(Vec l, float r) { return Vec.Mod(l, new Vec(l.size, r)); }
	
	public static Vec Cross(Vec l, Vec r) 
	{
		assert l.size == r.size : "Can't cross two vetors with different lengths.";
		assert l.size == 3 : "Cross product not defined for other than 3D.";
		return new Vec(3,
				l.values[1] * r.values[2] - r.values[1] * l.values[2], 
				r.values[0] * l.values[2] - l.values[0] * r.values[2],
				l.values[0] * r.values[1] - r.values[0] * l.values[1]);
	}
	
	public static float Dot(Vec l, Vec r) 
	{
		assert l.size == r.size : "Can't dot two vetors with different lengths.";
		int s = l.size;
		float dot = 0.0f;
		for (int i = 0; i < s; ++i) dot += l.values[i] * r.values[i];
		return dot;
	}
}
