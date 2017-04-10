package CRAP;

public class Vec3 {
	public float x,y,z;
	
	public Vec3() { x = y = z = 0.0f; }
	public Vec3(float v) { x = y = z = v; }
	public Vec3(float X, float Y, float Z) { x = X; y = Y; z = Z; }
	public Vec3(Vec3 v) { x = v.x; y = v.y; z = v.z; }
	
	////// Vec3 and Vec3 //////
	public static Vec3 Sum(Vec3 l, Vec3 r)
	{
		return new Vec3(l.x + r.x, l.y + r.y, l.z + r.z);
	}
	
	public static Vec3 Sub(Vec3 l, Vec3 r)
	{
		return new Vec3(l.x - r.x, l.y - r.y, l.z - r.z);
	}
	
	public static Vec3 Mul(Vec3 l, Vec3 r)
	{
		return new Vec3(l.x * r.x, l.y * r.y, l.z * r.z);
	}
	
	public static Vec3 Div(Vec3 l, Vec3 r)
	{
		return new Vec3(l.x / r.x, l.y / r.y, l.z / r.z);
	}
	
	public static Vec3 Mod(Vec3 l, Vec3 r) 
	{
		return new Vec3(l.x % r.x, l.y % r.y, l.z % r.z);
	}
	
	public boolean Equals(Vec3 v)
	{
		return x == v.x && y == v.y && z == v.z;
	}
	
	////// float and Vec3 //////
	public static Vec3 Sum(float l, Vec3 r) { return Vec3.Sum(new Vec3(l), r); }
	public static Vec3 Sub(float l, Vec3 r) { return Vec3.Sub(new Vec3(l), r); }
	public static Vec3 Mul(float l, Vec3 r) { return Vec3.Mul(new Vec3(l), r); }
	public static Vec3 Div(float l, Vec3 r) { return Vec3.Div(new Vec3(l), r); }
	public static Vec3 Mod(float l, Vec3 r) { return Vec3.Mod(new Vec3(l), r); }
	
	////// Vec3 and float //////
	public static Vec3 Sum(Vec3 l, float r) { return Vec3.Sum(l, new Vec3(r)); }
	public static Vec3 Sub(Vec3 l, float r) { return Vec3.Sub(l, new Vec3(r)); }
	public static Vec3 Mul(Vec3 l, float r) { return Vec3.Mul(l, new Vec3(r)); }
	public static Vec3 Div(Vec3 l, float r) { return Vec3.Div(l, new Vec3(r)); }
	public static Vec3 Mod(Vec3 l, float r) { return Vec3.Mod(l, new Vec3(r)); }
	
	public static Vec3 Cross(Vec3 l, Vec3 r) 
	{
		return new Vec3(
				l.y * r.z - r.y * l.z, 
				r.x * l.z - l.x * r.z,
				l.x * r.y - r.x * l.y);
	}
	
	public static float Dot(Vec3 l, Vec3 r) 
	{
		return l.x * r.x + l.y * r.y + l.z * r.z;
	}
	
}
