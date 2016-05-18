package solver;

import java.util.LinkedList;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import bean.Entity;
import bean.Path;
import net.sf.json.JSONArray;
import thread.AllIdThread;
import thread.AllRIdThread;
import thread.EntityThread;
import util.JsonUtil;
import util.Timer;

public class Id2IdSolver {
	private Entity source, sink;
	
	public Id2IdSolver() {}
	
	public Id2IdSolver(Entity source, Entity sink) {
		this.source = source;
		this.sink = sink;
	}

	public Entity getSource() {
		return source;
	}

	public void setSource(Entity source) {
		this.source = source;
	}

	public Entity getSink() {
		return sink;
	}

	public void setSink(Entity sink) {
		this.sink = sink;
	}

	public Path oneHop() {
		Path path = null;
		Set<Long> set = new HashSet<>(source.getRId());
		if (set.contains(sink.getId())) {
			path = new Path(source.getId(), sink.getId());
		}
		return path;
	}
	
	private List<Long> getIntersection(List<Long> x, List<Long> y) {
		Set<Long> set = new HashSet<>(y);
		return getIntersection(x, set);
	}
	
	private List<Long> getIntersection(List<Long> x, Set<Long> set) {
		List<Long> result = new LinkedList<Long>();
		for (Long a : x) {
			if (set.contains(a)){
				result.add(a);
			}
		}
		return result;
	}
	
	private Set<Long> toSet(List<Entity> list) {
		Set<Long> set = new HashSet<Long>();
		for (Entity entity : list) {
			set.add(entity.getId());
		}
		return set;
	}
	
	private void form2HopPaths(long source, List<Long> mid, long sink, List<Path> paths) {
		for (long item : mid) {
			paths.add(new Path(source, item, sink));
		}
	}
	
	public List<Path> twoHop(Entity source, Entity sink, Set<Long> rids) {
		List<Path> paths = new LinkedList<Path>();
		form2HopPaths(source.getId(), getIntersection(source.getFId(), sink.getFId()), sink.getId(), paths);
		form2HopPaths(source.getId(), getIntersection(source.getAuId(), sink.getAuId()), sink.getId(), paths);
		
		form2HopPaths(source.getId(), getIntersection(source.getRId(), rids), sink.getId(), paths);
		if (eqNotNone(source.getCId(), sink.getCId())) {
			paths.add(new Path(source.getId(), source.getCId(), sink.getId()));
		}
		if (eqNotNone(source.getJId(), sink.getJId())) {
			paths.add(new Path(source.getId(), source.getJId(), sink.getId()));
		}
		return paths;
	}
	
	private boolean eqNotNone(long x, long y) {
		return (x == y) && (x != JsonUtil.NONE);
	}
	
	private void form3HopPath(long source, List<Long> mid, Long mid2, long sink, List<Path> paths) {
		for (long item : mid) {
			paths.add(new Path(source, item, mid2, sink));
		}
	}
	
	public List<Path> threeHop(Entity source, Entity sink, List<Entity> sourceRids, List<Entity> sinkRids) {
		List<Path> paths = new LinkedList<Path>();
		long sourceId = source.getId(), sinkId = sink.getId();
		long cId = source.getCId(), jId = source.getJId();
		for (Entity item : sinkRids) {
			long itemId = item.getId();
			form3HopPath(sourceId, getIntersection(source.getFId(), item.getFId()), itemId, sinkId, paths);
			form3HopPath(sourceId, getIntersection(source.getAuId(), item.getAuId()), itemId, sinkId, paths);
			if (eqNotNone(item.getCId(), cId)) {
				paths.add(new Path(sourceId, cId, itemId, sinkId));
			}
			if (eqNotNone(item.getJId(), jId)) {
				paths.add(new Path(sourceId, jId, itemId, sinkId));
			}
		}
		
		Set<Long> midIds = toSet(sinkRids);
		for (Entity rid : sourceRids) {
			List<Path> twohops = twoHop(rid, sink, midIds);
			for (Path path : twohops) {
				path.pushNode(sourceId);
				paths.add(path);
			}
		}
		return paths;
	}
	
	public static void await(CountDownLatch latch) {
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public String getPath(long sourceId, long sinkId) {
//		source = new Entity();
//		sink = new Entity();
//		CountDownLatch latch = new CountDownLatch(2);
//		new EntityThread(latch, source, sourceId).start();
//		new EntityThread(latch, sink, sinkId).start();
//		await(latch);
		
		CountDownLatch latch = new CountDownLatch(2);
		List<Entity> sinkRids = Collections.synchronizedList(new LinkedList<Entity>());
		List<Entity> sourceRids = new LinkedList<>();
		new AllRIdThread(latch, sinkRids, sink).start();
		new AllIdThread(latch, source.getRId(), sourceRids).start();
		await(latch);
		
		List<Path> paths = twoHop(source, sink, toSet(sinkRids));
		List<Path> paths2 = threeHop(source, sink, sourceRids, sinkRids);
		JSONArray array = new JSONArray();
		Path tmp = oneHop();
		if (tmp != null) array.add(tmp.getPath());
		for (Path path : paths) array.add(path.getPath());
		for (Path path : paths2) array.add(path.getPath());
		return array.toString();
	}
	/**
     * Id = 2147152072
     * AuId = 676500258
     * Id = 1983578042
     * Id = 2178023902
     */
	//2157025439L, 2122841972
	//1502768748 2122841972
	//2147152072 2147152072
	//2292217923l 2100837269
	//2126125555l 2153635508l
	public static void main(String[] args) {
		Timer.init();
		Entity source = new Entity();
		Entity sink = new Entity();
		CountDownLatch latch = new CountDownLatch(2);
		new EntityThread(latch, source, 1502768748).start();
		new EntityThread(latch, sink, 2122841972).start();
		await(latch);
		Timer.log();
		Id2IdSolver solver = new Id2IdSolver(source, sink);
		List<Entity> sinkRids = Collections.synchronizedList(new LinkedList<Entity>());
		List<Entity> sourceRids = new LinkedList<>();
		latch = new CountDownLatch(2);
		new AllRIdThread(latch, sinkRids, sink).start();
		new AllIdThread(latch, source.getRId(), sourceRids).start();
		await(latch);
        Timer.log();
        List<Path> paths = solver.twoHop(source, sink, solver.toSet(sinkRids));
		List<Path> paths2 = solver.threeHop(source, sink, sourceRids, sinkRids);
		Timer.log();
		JSONArray array = new JSONArray();
		if (solver.oneHop() != null) {
			array.add(solver.oneHop().getPath());
		}
		for (Path path : paths) {
			array.add(path.getPath());
		}
		for (Path path : paths2) {
			array.add(path.getPath());
		}
		System.out.println("size=" + array.size());
		try {
			FileWriter writer = new FileWriter("out.json");
			writer.write(array.toString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println(array.toString());
	}
}
