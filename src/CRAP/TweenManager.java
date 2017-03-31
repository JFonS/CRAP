package CRAP;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;

public class TweenManager 
{
	ArrayList<Tween> tweenPool = new ArrayList<Tween>();
	
	public TweenManager()
	{
	}
	
	public void AddTween(Tween tween)
	{
		tweenPool.add(tween);
	}
	
	public void Update(float timeAbs)
	{
		Iterator<Tween> it = tweenPool.iterator();
		while (it.hasNext())
		{
			Tween tween = it.next();
			tween.Update(timeAbs);
			if (tween.HasFinished())
			{
				it.remove();
			}
		}
	}
}
