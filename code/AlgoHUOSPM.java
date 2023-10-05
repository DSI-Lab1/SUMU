import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author Gengsen Huang, 2022-4
 * This is the second version that uses two upper bounds (PEUO and RSUO)
 * and the minsup filtering strategy.
 */


public class AlgoHUOSPM {
	public long startTimestamp = 0;
	public long endTimestamp = 0;
	
	public double databaseUtility;
	public int minsup = 0;
	public double minuo = 0;
	
	public int huospCount = 0;
	public int totalSequence = 0;
	Map<Integer, Integer> mapItemToSUP;
	Map<Integer, Integer> mapSequenceToU;
	
	public int candidateCount = 0;
	BufferedWriter writer = null;


	/**
	 * Run HUOSPM algorithm
	 * 
	 * @param input
	 * @param output
	 * @param threshold
	 * 
	 * @throws IOException
	 */
	public void runAlgorithm(String input, String output, int minSup, double minUO) throws IOException {
		// reset maximum
		MemoryLogger.getInstance().reset();
		
		startTimestamp = System.currentTimeMillis();
		mapItemToSUP = new HashMap<Integer, Integer>();
		mapSequenceToU = new HashMap<Integer, Integer>();
		
		BufferedReader myInput = null;
		String thisLine = null;
		try {
			int sid = 0;
			myInput = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
			while ((thisLine = myInput.readLine()) != null) {
				sid++;
				if (thisLine.equals("")) {
					continue;
				}
				
				String[] spilts = thisLine.split("-2");
				int SUtility = Integer.parseInt(spilts[1].substring(spilts[1].indexOf(":") + 1));
				
				// update mapSequenceToU
				mapSequenceToU.put(sid, SUtility);
				// update the utility of each sequence
				databaseUtility += SUtility; 
				// update the number of sequences
				totalSequence += 1;  
				
				Set<Integer> tmpSet = new HashSet<Integer>();
				spilts[0] = spilts[0].substring(0, spilts[0].lastIndexOf("-1")).trim();
				String[] itemsetString = spilts[0].split("-1");
				
				// update mapItemToSUP
				for (String value : itemsetString) {
					String[] itemAndUtility = value.trim().split(" ");
					for (String val : itemAndUtility) {
						Integer item = Integer.parseInt(val.trim().substring(0, val.trim().indexOf("[")));
						if (!tmpSet.contains(item)) {
							Integer sup = mapItemToSUP.getOrDefault(item, 0);
							mapItemToSUP.put(item, sup + 1);
							tmpSet.add(item);
						}

					}
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				try {
					myInput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		minsup = minSup;
		minuo = minUO;
		List<Node> nodeList = new ArrayList<Node>();
		Map<Integer, Node> mapItemToNode = new HashMap<Integer, Node>();
		
		// Filter the unpromising 1-sequences: sup >= minsup
		for (Integer item : mapItemToSUP.keySet()) {
			if (mapItemToSUP.get(item) >= minsup) {
				Node node = new Node();
				node.addItemSet(String.valueOf(item));
				nodeList.add(node);
				mapItemToNode.put(item, node);
			}
		}
		
		
		Map<Integer, List<List<UItem>>> revisedDataBase = new HashMap<Integer, List<List<UItem>>>();
		
		// build the Utility-Occupancy-List-Chain
		try {
			int sid = 0;
			myInput = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
			while ((thisLine = myInput.readLine()) != null) {
				sid++;
				if (thisLine.equals("")) {
					continue;
				}
				String[] spilts = thisLine.split("-2");
				double remainingUO = 0;
				List<List<UItem>> sequence = new ArrayList<List<UItem>>();
				spilts[0] = spilts[0].substring(0, spilts[0].lastIndexOf("-1")).trim();
				String[] itemsetString = spilts[0].split("-1");
				
				// for each itemset/element
				for (String value : itemsetString) {
					String[] itemAndUtility = value.trim().split(" ");
					List<UItem> tmp = new ArrayList<UItem>();
					for (String val : itemAndUtility) {
						Integer item = Integer.parseInt(val.trim().substring(0, val.trim().indexOf("[")));
						Integer utility = Integer
								.parseInt(val.trim().substring(val.trim().indexOf("[") + 1, val.trim().indexOf("]")));
						Integer sup = mapItemToSUP.get(item);
						if (sup >= minsup) {
							UItem uItem = new UItem();
							uItem.setItem(item);
							uItem.setUO((double)utility / mapSequenceToU.get(sid));
							tmp.add(uItem);
							remainingUO += (double)utility / mapSequenceToU.get(sid);
						}
					}
					
					if (tmp.size() == 0) {
						continue;
					}
					
					// add this sequence
					sequence.add(tmp);
				}
				
				revisedDataBase.put(sid, sequence);
				int tid = 0;
				
				// for each sequence in the set of uitemList
				for (List<UItem> uitemList : sequence) {
					tid++;
					for (UItem uItem : uitemList) {
						Node node = mapItemToNode.get(uItem.getItem());
						UtilityOccupancyChain uoc = node.getUOC();
						Element element = new Element();
						element.setSid(sid);
						element.setTid(tid);
						element.setNext(-1);
						element.setAcuo(uItem.getUO());
						remainingUO = remainingUO - uItem.getUO();
						element.setRuo(remainingUO);
						uoc.addElement(sid, element);
					}
				}	
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				try {
					myInput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// for test
//		System.out.println("--------------Test Utility-Occupancy-List-Chain-----------------");
//	 	for(Node node:nodeList) {
//	 		node.printNode();
//	 	}
		
		// System.out.println("-------------------------------------------------");
		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
		// check the memory usage
	    MemoryLogger.getInstance().checkMemory();
		candidateCount += nodeList.size();
		
		
		// for each 1-sequences
		for (Node node : nodeList) {
			int sup = node.getSUP();
			double uo = node.getUO();
			if (uo >= minuo) {
				huospCount++;
				writeToFile(node.getPrefix(), uo, sup);
			}
			
			// call the HUOSPM function
			HUOSPM(node, revisedDataBase, minuo, minsup);
			
		}
			
		
		// check the memory usage
	    MemoryLogger.getInstance().checkMemory();
	    
		writer.close();
		endTimestamp = System.currentTimeMillis();
	}

	
	/**
	 * HUOSPM algorithm
	 * 
	 * @param node
	 * @param revisedDataBase
	 * @param minUtil
	 * @throws IOException
	 */
	private void HUOSPM(Node node, Map<Integer, List<List<UItem>>> revisedDataBase, double minuo, int minsup)
			throws IOException {
		
		// check the memory usage
	    MemoryLogger.getInstance().checkMemory();
		
		// prune early
		double peuo = node.getPEUO(minsup);
		if (peuo < minuo) {
			return;
		}
		
		// I-extension and S-extension
		List<List<Node>> lists = extension(node, revisedDataBase, minuo, minsup);
		
		// check the memory usage
	    MemoryLogger.getInstance().checkMemory();
	    
		// for each sequence in candidates
		if (lists != null) {

			// I-extension
			List<Node> iNode = lists.get(0);
			candidateCount += iNode.size();
			for (Node n : iNode) {
				int sup = n.getSUP();
				double uo = n.getUO();
				if (sup >= minsup && uo >= minuo) {
					huospCount++;
					writeToFile(n.getPrefix(), uo, sup);
				}
				
				// call the HUOSPM function
				if (sup >= minsup) {
					HUOSPM(n, revisedDataBase, minuo, minsup);
				}
				
			}

			// S-extension
			List<Node> sNode = lists.get(1);
			candidateCount += sNode.size();
			for (Node n : sNode) {
				int sup = n.getSUP();
				double uo = n.getUO();
				if (sup >= minsup && uo >= minuo) {
					huospCount++;
					writeToFile(n.getPrefix(), uo, sup);
				}
				
				// call the HUOSPM function
				if (sup >= minsup) {
					HUOSPM(n, revisedDataBase, minuo, minsup);
				}
			}
		}

	}

	public void printStats() {
		System.out.println("===========  HUOSPM ALGORITHM - STATS =========");
		System.out.println(" Total utility of DB: " + databaseUtility); 
		System.out.println(" minsup: "+ minsup);
		System.out.println(" minuo: "+ minuo);
		System.out.println(" Total time: " + (endTimestamp - startTimestamp)/1000.0 + " s");
		System.out.println(" Max memory: " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println(" HUOSPs: " + huospCount);
		System.out.println(" Candidates: " + candidateCount);
		System.out.println("===================================================");
	}

	
	/**
	 * Extension list
	 * 
	 * @param node
	 * @param revisedDataBase
	 * @param minUtil
	 * @return
	 */
	private List<List<Node>> extension(Node node, Map<Integer, List<List<UItem>>> revisedDataBase, double minuo, int minsup) {
		
		// the Utility-Occupancy-List-Chain of this node
		UtilityOccupancyChain uoc = node.getUOC();
		Map<Integer, List<Element>> uocMap = uoc.getUttilityOccupancyChain();
		List<String> sequence = node.getPrefix();
		Integer lastItem = getLastItem(sequence);
		Set<Integer> sidSet = uocMap.keySet();
		
		List<Integer> itemsForSExtension = new ArrayList<Integer>();
		List<Integer> itemsForIExtension = new ArrayList<Integer>();
		
		Map<Integer, Double> iMap = new HashMap<Integer, Double>();
		Map<Integer, Double> sMap = new HashMap<Integer, Double>();
		
		// calculate the ilist and slist with the PEUO upper bound
		for (Integer sid : sidSet) {
			List<List<UItem>> revisedSequence = revisedDataBase.get(sid);
			List<Element> elementList = uocMap.get(sid);
			
			
			double sPeuo = 0;
			// the initial Utility-Occupancy-List-Chain does not contain PEUO value
			if(uoc.getPEUO(sid) == null) {
				sPeuo = node.getPrefixPEUO(sid, minsup);
			}else {
				sPeuo = uoc.getPEUO(sid);
			}
			
			
			// I-extension
			Set<Integer> tmp = new HashSet<Integer>();
			for (Element e : elementList) {
				int tid = e.getTid().intValue();
				List<UItem> itemset = revisedSequence.get(tid - 1);
				for (UItem uitem : itemset) {
					Integer item = uitem.getItem();

					if(lastItem < item) {
					    tmp.add(item);
						if (!itemsForIExtension.contains(item)) {
							itemsForIExtension.add(item);

						}
					}
				}
			}
			
			// update the PEUO upper bound
			for (Integer key : tmp) {
				if (iMap.get(key) == null) {
					iMap.put(key, sPeuo);
				} else {
					iMap.put(key, iMap.get(key) + sPeuo);
				}
			}
			tmp.clear();
			
			
			// S-extension
			Element ele = elementList.get(0);
			Integer tid = ele.getTid();
			for (int i = tid.intValue(); i < revisedSequence.size(); i++) {
				List<UItem> itemsetList = revisedSequence.get(i);
				for (UItem u : itemsetList) {
					tmp.add(u.getItem());
					if (!itemsForSExtension.contains(u.getItem())) {
						itemsForSExtension.add(u.getItem());
					}
				}
			}
			
			// update the PEU upper bound
			for (Integer key : tmp) {
				if (sMap.get(key) == null) {
					sMap.put(key, sPeuo);
				} else {
					sMap.put(key, sMap.get(key) + sPeuo);
				}
			}
		}
		
		// Early prune
		if (itemsForSExtension.size() == 0 && itemsForIExtension.size() == 0) {
			return null;
		}

		// remove the unpromising item in the set of ilist
		for (Integer key : iMap.keySet()) {
			Double rsuValue = iMap.get(key);
			if (rsuValue < minuo) {
				itemsForIExtension.remove(key);
			}
		}
		
		// remove the unpromising item in the set of slist
		for (Integer key : sMap.keySet()) {
			Double rsuValue = sMap.get(key);
			if (rsuValue < minuo) {
				itemsForSExtension.remove(key);
			}
		}
		
		// construoct the Utility-Occupancy-List-Chain
		List<Node> iNodeList = new ArrayList<Node>();
		List<Node> sNodeList = new ArrayList<Node>();
		Map<Integer, Node> mapToINode = new HashMap<Integer, Node>();
		Map<Integer, Node> mapToSNode = new HashMap<Integer, Node>();
		
		// I-extension
		for (Integer item : itemsForIExtension) {
			Node n = new Node();
			for (int i = 0; i < sequence.size(); i++) {
				if (i != sequence.size() - 1) {
					n.getPrefix().add(sequence.get(i));
				} else {
					n.getPrefix().add(sequence.get(i) + "-" + item);
				}
			}
			iNodeList.add(n);
			mapToINode.put(item, n);
		}
		
		// S-extension
		for (Integer item : itemsForSExtension) {
			Node n = new Node();
			for (int i = 0; i < sequence.size(); i++) {
				n.getPrefix().add(sequence.get(i));
			}
			n.getPrefix().add(item + "");
			sNodeList.add(n);
			mapToSNode.put(item, n);
		}
		
		// scan each sid in the Utility-Occupancy-List-Chain (this projected DB)
		for (Integer sid : sidSet) {
			// scan the database
			List<List<UItem>> revisedSequence = revisedDataBase.get(sid);
			List<Element> elementList = uocMap.get(sid);
			
			/************************/
			double peuOfSID = 0;
			/************************/
			
			// for each element (subsequence) in sid
			for (Element e : elementList) {
				int tid = e.getTid().intValue();
				List<UItem> uItemset = revisedSequence.get(tid - 1);
				int indexOf = indexOf(lastItem, uItemset);
				double ru = e.getRuo();
				
				// I-extension
				for (int i = indexOf + 1; i < uItemset.size(); i++) {
					ru = ru - uItemset.get(i).getUO();
					
					if (itemsForIExtension.contains(uItemset.get(i).getItem())) {
						Element ele = new Element();
						ele.setSid(sid);
						ele.setTid(tid);
						ele.setNext(-1);
						ele.setAcuo(e.getAcuo() + uItemset.get(i).getUO());
						ele.setRuo(ru);
						
						Node node2 = mapToINode.get(uItemset.get(i).getItem());
						node2.getUOC().addElement(sid, ele);
						
						/**************************/
						if(peuOfSID < ele.getAcuo()+ ele.getRuo()) {
							peuOfSID = ele.getAcuo()+ ele.getRuo();
						}
						/**************************/
					} 
										
				}
				
				// S-extension
				for (int j = tid; j < revisedSequence.size(); j++) {
					List<UItem> uItemset2 = revisedSequence.get(j);
					for (UItem u : uItemset2) {
						ru = ru - u.getUO();
						
						if (itemsForSExtension.contains(u.getItem())) {
							Element ele = new Element();
							ele.setNext(-1);
							ele.setTid(j + 1);
							ele.setSid(sid);
							ele.setAcuo(e.getAcuo() + u.getUO());
							ele.setRuo(ru);
							
							Node node2 = mapToSNode.get(u.getItem());
							node2.getUOC().addElementForSExtendsion(sid, ele);
							
							/**************************/
							if(peuOfSID < ele.getAcuo()+ ele.getRuo()) {
								peuOfSID = ele.getAcuo()+ ele.getRuo();
							}
							/**************************/
						} 
					}
				}
				
				
			}
			
			
			
			/**************************/
			if(uoc.getPEUO(sid) != null) {
				uoc.setPEUO(sid, peuOfSID, minsup);
			}			
			/**************************/
		}
		
		// check the memory usage
	    MemoryLogger.getInstance().checkMemory();
	    
		List<List<Node>> lists = new ArrayList<List<Node>>();
		lists.add(iNodeList);
		lists.add(sNodeList);
		
		
		return lists;
	}

	/**
	 * Get last item in a sequence
	 * 
	 * @param sequence
	 * @return
	 */
	private Integer getLastItem(List<String> sequence) {
		String lastItemset = sequence.get(sequence.size() - 1);
		Integer lastItem = -1;
		if (!lastItemset.contains("-")) {
			lastItem = Integer.parseInt(lastItemset);
		} else {
			String[] splits = lastItemset.split("-");
			lastItem = Integer.parseInt(splits[splits.length - 1]);
		}
		
		return lastItem;
	}

	/**
	 * Index of item
	 * 
	 * @param item
	 * @param uList
	 * @return
	 */
	private int indexOf(Integer item, List<UItem> uList) {
		for (int i = 0; i < uList.size(); i++) {
			if (item == uList.get(i).getItem().intValue()) {
				return i;
			}
		}
		return -1;
	}

	private void writeToFile(List<String> sequence, double uo, int sup) throws IOException {
		String tmp = "";
		for (String str : sequence) {
			str = str.replace('-', ' ');
			tmp += str + " -1 ";
		}

		writer.write(tmp+ "#UOCC: "+ uo + " #SUP: " + sup);
		writer.newLine();
		writer.flush();
	}

}
