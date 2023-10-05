/**
 * @author Gengsen Huang, 2022-4
 * The implementation of the element of UOL-Chain.
 */


public class Element {
	private Integer sid;
	private Integer tid;
	private double acuo;
	private double ruo;
	private Integer next;

	public Integer getSid() {
		return sid;
	}

	public void setSid(Integer sid) {
		this.sid = sid;
	}

	public Integer getTid() {
		return tid;
	}

	public void setTid(Integer tid) {
		this.tid = tid;
	}

	public double getAcuo() {
		return acuo;
	}

	public void setAcuo(double acuo) {
		this.acuo = acuo;
	}

	public double getRuo() {
		return ruo;
	}

	public void setRuo(double ruo) {
		this.ruo = ruo;
	}

	public Integer getNext() {
		return next;
	}

	public void setNext(Integer next) {
		this.next = next;
	}

	@Override
	public String toString() {
		return "Element [sid=" + sid + ", tid=" + tid + ", acuo=" + acuo + ", ruo=" + ruo + ", next=" + next + "]";
	}

}
