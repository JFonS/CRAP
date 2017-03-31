package CRAP;

import interp.CRAPTree;
import interp.Interp;

public class Timeline implements Comparable
{
	private float startTimeAbs;
	private float finishTimeAbs;
	private String name;
	
	public Timeline(String name,
					float startTimeAbs, float finishTimeAbs)
	{
		assert startTimeAbs <= finishTimeAbs;
		
		this.name = name;
		this.startTimeAbs  = startTimeAbs;
		this.finishTimeAbs = finishTimeAbs;
	}

	@Override
	public int compareTo(Object objOther) 
	{
		Timeline other = (Timeline) objOther;
		return startTimeAbs < other.startTimeAbs ? -1 : 1;
	}
	
	public float GetStartTimeAbs() { return startTimeAbs; }
	public float GetFinishTimeAbs() { return finishTimeAbs; }
	public String GetName() { return name; }

}
