package CRAP;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;

public class TweenManager 
{
	ArrayList<Tween> tweenPool;
	
	public TweenManager()
	{
		tweenPool = new ArrayList<Tween>();
	}
	
	public void AddTween(Tween tween)
	{
		System.out.println("AddTween: " + tween);
		tweenPool.add(tween);
	}
	
	public void Update(float timeAbs)
	{
		Iterator<Tween> it = tweenPool.iterator();
		while (it.hasNext())
		{
			Tween tween = it.next();
			tween.Update(timeAbs);
			//System.out.println(tween.GetData().getNumberValue());
			if (tween.HasFinished())
			{
				it.remove();
			}
		}
	}
}
