package bean;

import java.util.LinkedList;
import java.util.List;

public class Path {
	private LinkedList<Long> path = new LinkedList<Long>();
	public Path(long source, long sink) {
		path.addLast(source);
		path.addLast(sink);
	}
	public Path(long source, long mid, long sink) {
		path.addLast(source);
		path.addLast(mid);
		path.addLast(sink);
	}
	public Path(long source, long mid1, long mid2, long sink) {
		path.addLast(source);
		path.addLast(mid1);
		path.addLast(mid2);
		path.addLast(sink);
	}
	public List<Long> getPath() {
		return path;
	}
	public void setPath(LinkedList<Long> path) {
		this.path = path;
	}
	public void appendNode(long node) {
		path.addLast(node);
	}
	public void pushNode(long node) {
		path.addFirst(node);
	}
	@Override
	public String toString() {
		return "Path [path=" + path + "]";
	}
}
