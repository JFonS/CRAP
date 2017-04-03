package CRAP;

import interp.CRAPTree;
import interp.Interp;

import java.util.Iterator;
import java.util.PriorityQueue;

public class TimelineManager
{
	Interp interpret;
	PriorityQueue<Timeline> timelineQueue;
	
	public TimelineManager(Interp interpret)
	{
		this.interpret = interpret;
		
		timelineQueue = new PriorityQueue<Timeline>();
	}
	
	public void Update(float timeAbs)
	{
		while (!timelineQueue.isEmpty() && 
				timeAbs >= timelineQueue.element().GetStartTimeAbs())
		{
			Timeline timeline = timelineQueue.element();
			System.out.println("  Executing " + timeline.ToString());
			interpret.executeTimeline(timeline);
			timelineQueue.remove();
		}
	}
	
	public void AddTimeline(Timeline timeline)
	{
		System.out.println("  Adding " + timeline.ToString());
		timelineQueue.add(timeline);
	}
}
