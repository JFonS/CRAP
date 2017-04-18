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
		System.out.println("Adding tween to list BEFORE: " + 
								(tweenList == null ? "[]" : tweenList.toString()));
		if (tweenList == null || tweenList.isEmpty())
		{
			tweenList = new ArrayList<Tween>(); 
			Tween beginTween = new Tween(tween.GetData(), Time.GetTimeAbs(), 
					                     new Data(tween.GetData()));
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
					
					ApplyTween(tweenStart, tweenEnd, t);
					
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
				Tween t = tweenList.get(0);
				if (timeAbs >= t.GetKeyTimeAbs())
				{
					Data d = t.GetData();
					d.setData(t.GetKeyData());
					tweenList.remove(0);
				}
			}
		}
	}
	
	private void ApplyTween(Tween tStart, Tween tEnd, float t)
	{
		Data dStart = tStart.GetKeyData();
		Data dEnd = tEnd.GetKeyData();
		
		if (dStart.isVec()) {
			Vec vStart = dStart.getVecValue();
			Vec vEnd = dEnd.getVecValue();
			assert vStart.Size() == vEnd.Size() : "Unmatching vector lengths in tween.";
			tStart.GetData().setValue(Vec.Sum(Vec.Mul(t, Vec.Sub(vEnd, vStart)), vStart));
		}
		else 
		{
			float vStart = tStart.GetKeyData().getNumberValue();
			float vEnd = tEnd.GetKeyData().getNumberValue();
			tStart.GetData().setValue( t * (vEnd - vStart) + vStart);
		}
	}
}
