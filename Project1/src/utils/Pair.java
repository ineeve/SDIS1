package utils;

import java.io.Serializable;
import java.util.HashSet;

public class Pair<L, R> implements Serializable, Comparable<Pair> {
	private static final long serialVersionUID = -7370372854326071958L;
	private L left;
	private R right;

	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}

	public L getLeft() {
		return left;
	}

	public R getRight() {
		return right;
	}
	
	public void setLeft(L newL) {
		left = newL;
	}

	public void setRight(R newR) {
		right = newR;
	}

	@Override
	public int hashCode() {
		return left.hashCode() ^ right.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Pair))
			return false;
		Pair<?, ?> pairo = (Pair<?, ?>) o;
		return this.left.equals(pairo.getLeft()) && this.right.equals(pairo.getRight());
	}

	@Override
	public int compareTo(Pair o) {
		Byte leftCasted = (Byte) left;
		HashSet<String> rightCasted = (HashSet<String>) right;
		Byte otherLeft = (Byte) o.left;
		HashSet<String> otherRight = (HashSet<String>) o.right;
		if (leftCasted != null && rightCasted != null && otherLeft != null && otherRight != null){
			int myDifference = rightCasted.size() - leftCasted;
			int otherDifference = otherRight.size() - otherLeft;
			return (myDifference < otherDifference ? 1 : myDifference > otherDifference ? -1 : 0);
		}
		return 0;
	}
}