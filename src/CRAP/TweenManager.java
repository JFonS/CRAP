package CRAP;

import CRAP.*;
import interp.*;
import java.util.*;

public class TweenManager 
{
	HashMap<Data, ArrayList<Tween>> tweenPool;
	
	public TweenManager()
	{
		tweenPool = new HashMap<Data, ArrayList<Tween>>();
	}
	
	public void AddTween(Tween tween)
	{
		ArrayList<Tween> tweenList = tweenPool.get(tween.GetData());
		if (tweenList == null) { tweenList = new ArrayList<Tween>(); }
		
		if (tweenList.size() == 0) { tweenList.add(tween); }
		for (int i = 0; i < tweenList.size(); ++i)
		{
			Tween other = tweenList.get(i);
			if (tween.GetFinishTimeAbs() < other.GetFinishTimeAbs())
			{
				tweenList.add(i, tween);
			}
		}
	}
	
	public void Update(float timeAbs)
	{
		for (ArrayList<Tween> tweenList : tweenPool.values())
		{
			Iterator<Tween> it = tweenList.iterator();
			while (it.hasNext())
			{
				Tween tween = it.next();
				tween.Update(timeAbs);
				
				// TODO, remove when the next one has finished too
				if (tween.HasFinished()) 
				{
					it.remove();
				}
			}
		}
	}
}
