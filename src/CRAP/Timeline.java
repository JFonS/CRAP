package CRAP;

import java.util.ArrayList;
import java.util.HashMap;

import interp.CRAPTree;
import interp.Data;
import interp.Interp;

public class Timeline implements Comparable
{
	private HashMap<String,Data> activationRecord;
	
	private float startTimeAbs;
	private float finishTimeAbs;
	private String name;
	private ArrayList<Data> args;
	
	public Timeline(String name, ArrayList<Data> args,
					float startTimeAbs, float finishTimeAbs)
	{
		assert startTimeAbs <= finishTimeAbs;
		
		this.name = name;
		this.startTimeAbs  = startTimeAbs;
		this.finishTimeAbs = finishTimeAbs;
		this.args = args;
	}

	@Override
	public int compareTo(Object objOther) 
	{
		Timeline other = (Timeline) objOther;
		return startTimeAbs < other.startTimeAbs ? -1 : 1;
	}
	
	public void SetActivationRecord(HashMap<String,Data> activationRecord)
	{
		this.activationRecord = activationRecord;
	}
	
	public String toString()
	{
		return "Timeline " + name + "[[" + startTimeAbs + ", " + finishTimeAbs + "]] args: (" + args + ")";
	}
	
	public float GetStartTimeAbs() { return startTimeAbs; }
	public float GetFinishTimeAbs() { return finishTimeAbs; }
	public ArrayList<Data> GetArgs() { return args; }
	public String GetName() { return name; }
	
	public ArrayList<Data> GetAliveDatas()
	{
		ArrayList<Data> aliveDatas = new ArrayList<Data>();
		for (Data aliveData : activationRecord.values())
		{
			aliveDatas.add(aliveData);
		}
		return aliveDatas;
	}

}
