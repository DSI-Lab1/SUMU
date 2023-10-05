/**
 * @author Gengsen Huang, 2022-4
 * The implementation of UO-Table.
 * Each node owns the following information.
 * prefix: prefix
 * sup: getSUP()
 * uo: getUO()
 * uoc: UOL-Chain
 */


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Node {
	private List<String> prefix = new ArrayList<>();
	private UtilityOccupancyChain uoc = new UtilityOccupancyChain();

	public List<String> getPrefix() {
		return prefix;
	}

	public void addItemSet(String itemset) {
		prefix.add(itemset);
	}

	public UtilityOccupancyChain getUOC() {
		return uoc;
	}

	public double getUO() {
		Map<Integer, List<Element>> uttilityChain = uoc.getUttilityOccupancyChain();
		int sup = uttilityChain.size();
		double uo = 0;
		for (Map.Entry<Integer, List<Element>> me : uttilityChain.entrySet()) {
			List<Element> valueList = me.getValue();
			double max = -9999;
			for (Element value : valueList) {
				if (value.getAcuo() > max) {
					max = value.getAcuo();
				}
			}
			uo += max;
		}
		return uo / sup;
	}

	public double getPEUO(int minsup) {
		double peuo = 0;
		Map<Integer, List<Element>> uttilityChain = uoc.getUttilityOccupancyChain();
		for (Map.Entry<Integer, List<Element>> me : uttilityChain.entrySet()) {
			List<Element> valueList = me.getValue();
			double max = -9999;
			for (Element value : valueList) {
				double tmp = 0;
				if (value.getRuo() > 0) {
					tmp = value.getAcuo() + value.getRuo();
				}
				if (tmp > max) {
					max = tmp;
				}
			}
			peuo += max;
		}
		return peuo / minsup;
	}
	
	class MyComparator implements Comparator<Double>{

		@Override
		public int compare(Double o1, Double o2) {
			if(o1 - o2 == 0 || o1 > o2)return 1;
			else return -1;
		}
		
	}
	
	public double getTPUO(int minsup) {
		double tpuo = 0;
		PriorityQueue<Double> peuo = new PriorityQueue<Double> (minsup, new MyComparator());
		Map<Integer, List<Element>> uttilityChain = uoc.getUttilityOccupancyChain();
		for (Map.Entry<Integer, List<Element>> me : uttilityChain.entrySet()) {
			List<Element> valueList = me.getValue();
			double max = -9999;
			for (Element value : valueList) {
				double tmp = 0;
				if (value.getRuo() > 0) {
					tmp = value.getAcuo() + value.getRuo();
				}
				if (tmp > max) {
					max = tmp;
				}
			}
			if(peuo.size() == minsup){
				if(max > peuo.peek()) {
					peuo.poll();
					peuo.add(max);
				}
				
			}else {
				peuo.add(max);
			}
		}
		
		while(!peuo.isEmpty()) {
			tpuo += peuo.poll();
		}
		return tpuo / minsup;
	}
	
	
	
	public boolean judge(int minsup, double minuo) {
		double peuo = 0;
		int pes = 0;
		Map<Integer, List<Element>> uttilityChain = uoc.getUttilityOccupancyChain();
		for (Map.Entry<Integer, List<Element>> me : uttilityChain.entrySet()) {
			List<Element> valueList = me.getValue();
			double max = -9999;
			for (Element value : valueList) {
				double tmp = 0;
				if (value.getRuo() > 0) {
					tmp = value.getAcuo() + value.getRuo();
				}
				if (tmp > max) {
					max = tmp;
				}
			}
			peuo += max;
			if(max > 0) {
				pes++;
			}
		}
		if(pes < minsup || peuo / minsup < minuo) {
			return true;
		}
		return false;
	}
	
	
	
	/**
	 * Calculate the PEUO value of each sid (prefix)
	 * 
	 * @param sid
	 * @return
	 */
	@SuppressWarnings("null")
	public double getPrefixPEUO(Integer sid, int minsup) {
		Map<Integer, List<Element>> uttilityChain = uoc.getUttilityOccupancyChain();
		List<Element> elementList = uttilityChain.get(sid);
		
		// for each element
		if (elementList != null || elementList.size() > 0) {
			double max = -9999;
			
			// for each item in this element
			for (Element value : elementList) {
				double tmp = 0;
				if (value.getRuo() > 0) {
					tmp = value.getAcuo() + value.getRuo();
				}
				if (tmp > max) {
					max = tmp;
				}
			}
			return max / minsup;
		}
		return 0;
	}
	
	

	public void printNode() {
		String str = "";
		for (String s : prefix) {
			str += s + ";";
		}
		str = str.substring(0, str.length() - 1);
		System.out.println("---------------" + str + "-----------------");
		Map<Integer, List<Element>> uttilityChain = uoc.getUttilityOccupancyChain();
		for (Map.Entry<Integer, List<Element>> me : uttilityChain.entrySet()) {
			List<Element> value = me.getValue();
			for (Element e : value) {
				System.out.print(e.getSid() + " " + e.getTid() + " " + e.getAcuo() + " " + e.getRuo() + " " + e.getNext()
						+ "--->");
			}
			System.out.println();
		}
		System.out.println("---------------" + str + "-----------------");
	}

	
	public int getSUP() {
		int sup = uoc.getUttilityOccupancyChain().size();
		return sup;
	}
	
	public int getPEUOS() {
		int peuoSup = 0;
		Map<Integer, List<Element>> uttilityChain = uoc.getUttilityOccupancyChain();
		for (Map.Entry<Integer, List<Element>> me : uttilityChain.entrySet()) {
			List<Element> valueList = me.getValue();
			for (Element value : valueList) {
				if (value.getRuo() > 0) {
					peuoSup++;
					break;
				}
			}
		}
		return peuoSup;
	}
}
