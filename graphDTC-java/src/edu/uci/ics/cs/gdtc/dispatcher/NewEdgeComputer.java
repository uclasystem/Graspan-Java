/**ALTERNATE INTERFACE FOR NEW EDGE COMPUTATION. (INCOMPLETE), OCT 26 2015
*
*/

package edu.uci.ics.cs.gdtc.dispatcher;
import java.io.IOException;

import edu.uci.ics.cs.gdtc.engine.BasicScheduler;
import edu.uci.ics.cs.gdtc.engine.PartitionLoader;

public class NewEdgeComputer {
	
	public static void main(String args[]) throws IOException {

		String baseFilename = args[0];
		int numInParts = Integer.parseInt(args[1]);
		int numPartsPerComputation = Integer.parseInt(args[2]);
		/**
		 * SCHEDULING AND LOADING (LOAD PARTITIONS TO MEMORY)
		 */
		System.out.println("Start scheduling and loading");
		System.out.print("Initializing scheduler... ");
		BasicScheduler basicScheduler = new BasicScheduler();
		basicScheduler.initScheduler(numInParts);
		System.out.print("Done\n");

		System.out.print("Initializing loader... ");
		PartitionLoader partLoader = new PartitionLoader();
		System.out.print("Done\n");

		// TODO use a loop here as determined by scheduler
		long loadingStartTime = System.nanoTime();
		partLoader.loadParts(baseFilename, basicScheduler.getPartstoLoad(numPartsPerComputation),
				numInParts);
		long loadingDuration = System.nanoTime() - loadingStartTime;
		System.out.println(
				">Total time for loading " + numPartsPerComputation + " partitions (nanoseconds): " + loadingDuration);

		/**
		 * COMPUTATION (GENERATE NEW EDGES)
		 */
		// compute new edges
		// PartitionLoader newEdgeComputer = new PartitionLoader();
		// newEdgeComputer.computeNewEdges(partLoader.partEdgeArrays,
		// partLoader.partEdgeValArrays,
		// partLoader.partOutDegrees);

		// compute new edges from the partitions loaded
	}
}
