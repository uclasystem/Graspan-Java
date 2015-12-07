package edu.uci.ics.cs.graspan.scheduler;

import java.util.ArrayList;
import java.util.List;

import edu.uci.ics.cs.graspan.datastructures.LoadedVertexInterval;


/**
 * @author Kai Wang
 *
 * Created by Oct 30, 2015
 */
public class Scheduler {
	private List<PartitionEdgeInfo> allEdgeInfo = new ArrayList<PartitionEdgeInfo>();
	private List<Long> partitionNumEdges = new ArrayList<Long>();
	private List<LoadedVertexInterval> intervals = null;
	
	// for temp use
	public static int counter;
	public static int numOfPartitions;
	
	/**
	 * Constructor
	 * @param partSizes
	 * @param edgeDstCount
	 * @exception 
	 */
	public Scheduler(long[] partSizes, long[][] edgeDstCount) {
		if(partSizes == null || edgeDstCount == null)
			throw new IllegalArgumentException("Null parameter in scheduler!");
		
		for(long size : partSizes)
			partitionNumEdges.add(Long.valueOf(size));
		
		for(int i = 0; i < edgeDstCount.length; i++) {
			PartitionEdgeInfo edgeInfo = new PartitionEdgeInfo(i, edgeDstCount[i]);
			allEdgeInfo.add(edgeInfo);
		}
		
	}
	
	// only consider termination (without scheduling)
	public Scheduler(int numParts){
//	public Scheduler(int[] allPartitions) {
//		if(allPartitions == null)
//			throw new IllegalArgumentException("Null parameter in scheduler!");
		
		for(int i = 0; i < numParts; i++) {
			PartitionEdgeInfo edgeInfo = new PartitionEdgeInfo(i, numParts);
			allEdgeInfo.add(edgeInfo);
		}
		
		numOfPartitions = numParts;
	}
	
	public void setLoadedIntervals(List<LoadedVertexInterval> intervals) {
		this.intervals = intervals;
	}
	
	public List<LoadedVertexInterval> getLoadedIntervals() {
		return intervals;
	}
	
	private void computePriority() {
		assert(allEdgeInfo != null && partitionNumEdges != null);
		assert(allEdgeInfo.size() > 0 && partitionNumEdges.size() > 0);
		int size = allEdgeInfo.size();
		
		// each partition has an edgeInfo, iterate all partitions
		for(PartitionEdgeInfo edgeInfo : allEdgeInfo) {
			int partitionId = edgeInfo.getPartitionId();
			// get edgeInfo and priorityInfo for each partition
			List<Long> pEdgeInfo = edgeInfo.getPartitionEdgeInfo();
			List<Long> priorityInfo = edgeInfo.getPriorityInfo();
			
			// compute priority, starting from (partitionId + 1)
			// for example, if there are 3 partitions, 0, 1, 2,
			// the current partition is 0, then consider(p0, p1), (p0, p2)
			for(int i = (partitionId + 1); i < size; i++) {
				// get edgeInfo for the current partition
				long edgesCurrentPartition = pEdgeInfo.get(partitionId);
				// get edgeInfo for the next partition
				long edgesNextPartition = pEdgeInfo.get(i);
				// compute priority and set priority
				priorityInfo.set(i, edgesCurrentPartition + edgesNextPartition);
			}
		}
		
	}
	
	/**
	 * 
	 * Description: select two partitions to load
	 * @param:
	 * @return:
	 */
	public List<Integer> schedulePartitions() {
		computePriority();
		long maxPriority = 0L;
		int scheduledOne = -1;
		int scheduledTwo = -1;
		
		// schedule two partitions every time
		List<Integer> result = new ArrayList<Integer>(2);
		
		// each partition has an edgeInfo
		for(PartitionEdgeInfo edgeInfo : allEdgeInfo) {
			int partitionId = edgeInfo.getPartitionId();
			scheduledOne = partitionId;
			List<Long> priorityInfo = edgeInfo.getPriorityInfo();
			for(int i = 0; i < priorityInfo.size(); i++) {
				if(priorityInfo.get(i) > maxPriority) {
					maxPriority = priorityInfo.get(i);
					scheduledTwo = i;
				}
			}
		}
		
		result.add(scheduledOne);
		result.add(scheduledTwo);
		return result;
	}
	
	// A very simple sequential scheduler
	public int[] schedulePartitionSimple() {
		if(counter >= numOfPartitions)
			counter = 0;
		// schedule two partitions every time
		int[] scheduled = new int[2];
		scheduled[0] = counter++;
		scheduled[1] = counter++;
		
		return scheduled;
	}
	
	/**
	 * 
	 * Description:check whether to be terminated. call it before every iteration.
	 * @param:
	 * @return:boolean
	 */
	public boolean shouldTerminate() {
		int size = allEdgeInfo.size();
		
		for(int i = 0; i < allEdgeInfo.size(); i++) {
			int partitionId = allEdgeInfo.get(i).getPartitionId();
			List<Boolean> terminationInfo = allEdgeInfo.get(i).getTerminationInfo();
			for(int j = (partitionId + 1); j < size; j++) {
				if(!terminationInfo.get(j))
					return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 
	 * Description: set termination status every time after computation.
	 * @param:
	 * @return:void
	 */
	public void setTerminationStatus() {
		assert(intervals.size() == 2);
		
		int loadedPartitionOne = intervals.get(0).getPartitionId();
		boolean isNewEdgeAddedForOne = intervals.get(0).hasNewEdges();
		int loadedPartitionTwo = intervals.get(1).getPartitionId();
		boolean isNewEdgeAddedForTwo = intervals.get(1).hasNewEdges();
		
		List<Boolean> terminationInfoForOne = allEdgeInfo.get(loadedPartitionOne).getTerminationInfo();
		List<Boolean> terminationInfoForTwo = allEdgeInfo.get(loadedPartitionTwo).getTerminationInfo();
		
		if(isNewEdgeAddedForOne) {
			// set the row to false
			for(int i = 0; i < terminationInfoForOne.size(); i++)
				terminationInfoForOne.set(i, false);
			
			// set the column to false
			for(int i = 0; i < allEdgeInfo.size(); i++) 
				allEdgeInfo.get(i).getTerminationInfo().set(loadedPartitionTwo, false);
		}
		
		if(isNewEdgeAddedForTwo) {
			// set the row to false
			for(int i = 0; i < terminationInfoForTwo.size(); i++)
				terminationInfoForTwo.set(i, false);
			
			// set the column to false
			for(int i = 0; i < allEdgeInfo.size(); i++)
				allEdgeInfo.get(i).getTerminationInfo().set(loadedPartitionOne, false);
		}
		
		terminationInfoForOne.set(loadedPartitionTwo, true);
		terminationInfoForTwo.set(loadedPartitionOne, true);
	}
	
	// update termination status after repartitioning
	public void updateSchedulingInfo(int numOfNewPartitions, int totalPartitions) {
		for(PartitionEdgeInfo info : allEdgeInfo) {
			for(int i = 0; i < numOfNewPartitions; i++) {
				info.getTerminationInfo().add(Boolean.FALSE);
			}
		}
		
		int numOfOldPartitions =  totalPartitions - numOfNewPartitions;
		for(int i = 0; i < numOfNewPartitions; i++) {
			PartitionEdgeInfo newInfo = new PartitionEdgeInfo(numOfOldPartitions + i, totalPartitions);
			allEdgeInfo.add(newInfo);
		}
	}
}