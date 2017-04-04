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

		System.out.println("Add tween!"); 
		ArrayList<Tween> tweenList = tweenPool.get(tween.GetData());
		System.out.println("Adding tween to list BEFORE: " + 
								(tweenList == null ? "[]" : tweenList.toString()));
		if (tweenList == null || tweenList.isEmpty())
		{
			tweenList = new ArrayList<Tween>(); 
			Tween beginTween = new Tween(tween.GetData(), Time.GetTimeAbs(), 
					                     tween.GetData().getNumberValue());
			tweenList.add(beginTween); 
			tweenList.add(tween);
			tweenPool.put(tween.GetData(), tweenList);
		}
		else
		{
			boolean foundPlace = false;
			for (int i = 0; i < tweenList.size(); ++i)
			{
				Tween other = tweenList.get(i);
				if (tween.GetKeyTimeAbs() < other.GetKeyTimeAbs())
				{
					tweenList.add(i, tween);
					foundPlace = true;
					break;
				}
			}
			if (!foundPlace) { tweenList.add(tween); } // Add to the end
		}

		System.out.println("Adding tween to list AFTER: " + tweenList.toString());
		
	}
	
	public void Update()
	{
		float timeAbs = Time.GetTimeAbs();
		for (Data tweenData : tweenPool.keySet())
		{
			// First tween the data
			ArrayList<Tween> tweenList = tweenPool.get(tweenData);
			for (int i = 0; i < tweenList.size() - 1; ++i)
			{
				Tween tweenStart = tweenList.get(i);
				Tween tweenEnd   = tweenList.get(i+1);
				if (timeAbs >= tweenStart.GetKeyTimeAbs() &&
					timeAbs <= tweenEnd.GetKeyTimeAbs())
				{
					float startTime = tweenStart.GetKeyTimeAbs();
					float endTime   = tweenEnd.GetKeyTimeAbs();
					float t = (timeAbs - startTime) / (endTime - startTime);
					
					float vStart = tweenStart.GetKeyValue();
					float vEnd   = tweenEnd.GetKeyValue();
					tweenData.setValue( t * (vEnd - vStart) + vStart);
					
					break;
				}
			}
			
			// Then remove outdated tweens
			for (int i = 0; i < tweenList.size() - 1; ++i)
			{
				Tween tweenEnd   = tweenList.get(i+1);
				if (timeAbs >= tweenEnd.GetKeyTimeAbs())
				{
					tweenList.remove(i); --i;
				}
			}
			if (tweenList.size() == 1)
			{
				if (timeAbs >= tweenList.get(0).GetKeyTimeAbs())
				{
					tweenList.remove(0);
				}
			}
		}
	}
	
	public ArrayList<Data> GetAliveDatas()
	{
		ArrayList<Data> aliveDatas = new ArrayList<Data>();
		aliveDatas.addAll(tweenPool.keySet());
		System.out.println("ALIVE TWEEN DATAS:" + aliveDatas);
		return aliveDatas;
	}
}
