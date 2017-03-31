package CRAP;

import interp.*;

public class Tween
{
	private Data data;
	private float startTimeAbs, finishTimeAbs;
	private float vStart, vFinish;
	
	private boolean tweenStarted = false, tweenFinished = false;
	
	public Tween(Data data, float startTimeAbs, float finishTimeAbs, float vFinish)
	{
		assert data.isNumber();
		assert startTimeAbs <= finishTimeAbs;
		
		this.data          = data;
		this.startTimeAbs  = startTimeAbs;
		this.finishTimeAbs = finishTimeAbs;
		this.vFinish       = vFinish;
	}
	
	public void Update(float timeAbs)
	{
		tweenFinished = timeAbs >= finishTimeAbs;
		
		if (timeAbs < startTimeAbs) { return; } 
		if (tweenFinished) 
		{ 
			data.setValue(vFinish);
			return; 
		}
		
		
		if (!tweenStarted)
		{
			tweenStarted = true;
			vStart = data.getNumberValue();
		}

		float t = 1.0f - (finishTimeAbs - timeAbs) / (finishTimeAbs - startTimeAbs);
		data.setValue(vStart + (vFinish - vStart) * t);
	}
	
	public Data GetData() { return data; }
	
	public boolean HasStarted()  { return tweenStarted; }
	public boolean HasFinished() { return tweenFinished; }
}
