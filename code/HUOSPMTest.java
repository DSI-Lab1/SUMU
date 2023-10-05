import java.io.IOException;

/**
 * @author Gengsen Huang, 2022-4
 * This test is for high-utility occupancy sequential pattern mining
 * AlgoHUOSPM.java is the proposed algorithm named SUMU.
 */

public class HUOSPMTest {
	public static void main(String[] args) throws IOException {
		
		String input = "./data/" + args[0] + ".txt"; 
		// the path for saving the patterns found
		String output = ".//output.txt";  
		// the minimum support
		int minsup = Integer.parseInt(args[1]);
		// the minimum utility occupancy, (0 < minuo <= 1)
		double minuo = Double.parseDouble(args[2]);
        
		AlgoHUOSPM_PES algo3 = new AlgoHUOSPM_PES();
		algo3.runAlgorithm(input, output, minsup, minuo);
                algo3.printStats();

	}
}
