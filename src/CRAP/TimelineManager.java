package CRAP;

import interp.CRAPTree;
import interp.Data;
import interp.Interp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;

public class TimelineManager
{
	Interp interpret;
	ArrayList<Timeline> executedTimelines;
	PriorityQueue<Timeline> timelineQueue;
	
	public TimelineManager(Interp interpret)
	{
		this.interpret = interpret;
		executedTimelines = new ArrayList<Timeline>();
		timelineQueue = new PriorityQueue<Timeline>();
	}
	
	public void Update()
	{
		float timeAbs = Time.GetTimeAbs();
		
		// Execute timelines
		while (!timelineQueue.isEmpty() && 
				timeAbs >= timelineQueue.element().GetStartTimeAbs())
		{
			Timeline timeline = timelineQueue.element();
			System.out.println("Executing " + timeline);
			interpret.executeTimeline(timeline);
			
			executedTimelines.add(timeline);
			timelineQueue.remove();
		}
		
		// Removed timelines that have expired
		Iterator<Timeline> execTimelineIt = executedTimelines.iterator();
		while (execTimelineIt.hasNext())
		{
			Timeline execTimeline = (Timeline) execTimelineIt.next();
			if (timeAbs >= execTimeline.GetFinishTimeAbs())
			{
				execTimelineIt.remove();
			}
		}
	}
	
	public void AddTimeline(Timeline timeline)
	{
		System.out.println("  Adding " + timeline.toString());
		timelineQueue.add(timeline);
	}

	public ArrayList<Data> GetAliveDatas()
	{
		ArrayList<Data> aliveDatas = new ArrayList<Data>();
		for (Timeline timeline : executedTimelines)
		{
			ArrayList<Data> timelineAliveDatas = timeline.GetAliveDatas();
			aliveDatas.addAll(timelineAliveDatas);
		}
		return aliveDatas;
	}
}
