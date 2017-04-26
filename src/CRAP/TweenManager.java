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
					ApplyTween(tweenStart, tweenEnd, timeAbs);
					
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
	
	private void ApplyTween(Tween tStart, Tween tEnd, float timeAbs)
	{
		Data dStart = tStart.GetKeyData();
		Data dEnd = tEnd.GetKeyData();
		Data dataToTween = tStart.GetData();
		
		float t = timeAbs - tStart.GetKeyTimeAbs();
		float d = tEnd.GetKeyTimeAbs() - tStart.GetKeyTimeAbs();
		
		if (dStart.isVec()) {
			Vec vStart = dStart.getVecValue();
			Vec vEnd = dEnd.getVecValue(); //dEnd.isVec() ? dEnd.getVecValue() : new Vec(vStart.Size(),dEnd.getNumberValue());
			assert vStart.Size() == vEnd.Size() : "Unmatching vector lengths in tween.";
		
			System.out.println(vStart + " -> " + vEnd + "[[" + dataToTween + "]]");
			
			for (int i = 0; i < vStart.Size(); ++i) 
			{
				float b = vStart.Get(i);
				float c = vEnd.Get(i) - b;
				dataToTween.getVecValue().Set(i, EaseValue(tEnd.GetInterpType(), t, b, c, d));
			}
		}
		else 
		{
			float b = dStart.getNumberValue();
			float c = dEnd.getNumberValue() - b;
			dataToTween.setValue(EaseValue(tEnd.GetInterpType(),t,b,c,d));
		}
	}
	
	private float EaseValue(String function, float t, float b, float c, float d) {		
		//System.out.format("%f,%f,%f,%f %n",t,b,c,d);
		switch (function) {
			case "Cubic": 
				if ((t/=d/2) < 1) return c/2*t*t*t + b;
				return c/2*((t-=2)*t*t + 2) + b;
			
			case "Elastic":
				if (t==0) return b;  if ((t/=d)==1) return b+c;  
				float p=d*.3f;
				float a=c; 
				float s=p/4;
				return (a*(float)Math.pow(2,-10*t) * (float)Math.sin( (t*d-s)*(2*(float)Math.PI)/p ) + c + b); 
		}
		
		//Linear
		return c*t/d + b;
	}
}
