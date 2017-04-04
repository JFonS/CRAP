package CRAP;

public class Time 
{
	private static long init;
	private static float timeAbs;
	
	public static void Init()
	{
		init = System.currentTimeMillis();
		timeAbs = 0.0f;
	}
	
	public static void Update()
	{
		timeAbs = (System.currentTimeMillis() - init) / 1000.0f;
	}
	
	public static float GetTimeAbs() 
	{
		return timeAbs;
	}
}
