/**
 * @author Gengsen Huang, 2022-4
 * The implementation of UOL-Chain.
 */


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtilityOccupancyChain {
	private Map<Integer, List<Element>> uoc = new HashMap<Integer, List<Element>>();

	public void addElements(Integer sid, List<Element> elements) {
		uoc.put(sid, elements);
	}
	
	// new peuo value of each sid
	private Map<Integer, Double> peuo  = new HashMap<Integer, Double>(); 

	/**
	 * Add element
	 * 
	 * @param sid
	 * @param element
	 */
	public void addElement(Integer sid, Element element) {
		List<Element> list = this.uoc.get(sid);
		if (list == null || list.size() == 0) {
			List<Element> elements = new ArrayList<Element>();
			elements.add(element);
			addElements(sid, elements);
		} else {
			Element ele = list.get(list.size() - 1);
			ele.setNext(list.size());
			list.add(element);
			uoc.put(sid, list);
		}
	}

	/**
	 * Add element for S-extension
	 * 
	 * @param sid
	 * @param element
	 */
	public void addElementForSExtendsion(Integer sid, Element element) {
		List<Element> list = this.uoc.get(sid);
		if (list == null || list.size() == 0) {
			// the first item
			List<Element> elements = new ArrayList<Element>();
			elements.add(element);
			addElements(sid, elements);
		} else {
			int index = -1;
			for (int i = 0; i < list.size(); i++) {
				Element e = list.get(i);
				if (e.getTid().intValue() == element.getTid().intValue()) {
					index = i;
					break;
				}
			}
			if (index != -1) {
				Element e = list.get(index);
				
				// update the acuo value
				if (e.getAcuo() < element.getAcuo()) {
					e.setAcuo(element.getAcuo());
				}
			} else {
				Element ele = list.get(list.size() - 1);
				ele.setNext(list.size());
				list.add(element);
				uoc.put(sid, list);
			}
		}
	}

	public Map<Integer, List<Element>> getUttilityOccupancyChain() {
		return this.uoc;
	}
	

	public Double getPEUO(Integer sid) {
		return peuo.get(sid);
	}

	public void setPEUO(Integer sid, double peuo, int minsup) {
		this.peuo.put(sid, peuo / minsup);
	}

}
