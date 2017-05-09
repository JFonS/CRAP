package CRAP;

import CRAP.easing.*;
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
		//System.out.println("Adding tween to list BEFORE: " + 
		//						(tweenList == null ? "[]" : tweenList.toString()));
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

		// System.out.println("Adding tween to list AFTER: " + tweenList.toString());
		
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
			case "BackIn":
				return Back.easeIn(t, b, c, d);
			case "BackOut":
				return Back.easeOut(t, b, c, d);
			case "Back":
			case "BackInOut":
				return Back.easeInOut(t, b, c, d);
			
			case "BounceIn":
				return Bounce.easeIn(t, b, c, d);
			case "BounceOut":
				return Bounce.easeOut(t, b, c, d);
			case "Bounce":
			case "BounceInOut":
				return Bounce.easeInOut(t, b, c, d);
				
			case "CircIn":
				return Circ.easeIn(t, b, c, d);
			case "CircOut":
				return Circ.easeOut(t, b, c, d);
			case "Circ":
			case "CircInOut":
				return Circ.easeInOut(t, b, c, d);
			
			case "CubicIn":
				return Cubic.easeIn(t, b, c, d);
			case "CubicOut":
				return Cubic.easeOut(t, b, c, d);
			case "Cubic":
			case "CubicInOut":
				return Cubic.easeInOut(t, b, c, d);
				
			case "ElasticIn":
				return Elastic.easeIn(t, b, c, d);
			case "ElasticOut":
				return Elastic.easeOut(t, b, c, d);
			case "Elastic":
			case "ElasticInOut":
				return Elastic.easeInOut(t, b, c, d);
				
			case "ExpoIn":
				return Expo.easeIn(t, b, c, d);
			case "ExpoOut":
				return Expo.easeOut(t, b, c, d);
			case "Expo":
			case "ExpoInOut":
				return Expo.easeInOut(t, b, c, d);
				
			case "QuadIn":
				return Quad.easeIn(t, b, c, d);
			case "QuadOut":
				return Quad.easeOut(t, b, c, d);
			case "Quad":
			case "QuadInOut":
				return Quad.easeInOut(t, b, c, d);
				
			case "QuartIn":
				return Quart.easeIn(t, b, c, d);
			case "QuartOut":
				return Quart.easeOut(t, b, c, d);
			case "Quart":
			case "QuartInOut":
				return Quart.easeInOut(t, b, c, d);
				
			case "QuintIn":
				return Quint.easeIn(t, b, c, d);
			case "QuintOut":
				return Quint.easeOut(t, b, c, d);
			case "Quint":
			case "QuintInOut":
				return Quint.easeInOut(t, b, c, d);
				
			case "SineIn":
				return Sine.easeIn(t, b, c, d);
			case "SineOut":
				return Sine.easeOut(t, b, c, d);
			case "Sine":
			case "SineInOut":
				return Sine.easeInOut(t, b, c, d);
		}
		
		//Linear
		return Linear.easeNone(t, b, c, d);
	}
}
