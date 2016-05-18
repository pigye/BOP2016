package solver;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import util.JsonUtil;
import util.Timer;
import bean.AuIdandAfId;
import bean.Author;
import bean.Entity;
import bean.Path;
import net.sf.json.JSONArray;
import thread.AllRIdThread;
import thread.AuthorThread;
import thread.EntityThread;
import thread.SetThread;

public class AuId2IdSolver {
	
	private Author source;
	private Entity  sink;
	
	public AuId2IdSolver() {}
	
	public AuId2IdSolver(Author source, Entity sink) {
		this.source = source;
		this.sink = sink;
	}

	public AuId2IdSolver(Entity sink) {
		this.sink = sink;
	} 

	public Path oneHop(Author source,Entity  sink) {
		Path path = null;
		Set<Long> set = new HashSet<>(sink.getAuId());
		if (set.contains(source.getAuId())) {
			path = new Path(source.getAuId(), sink.getId());
		}
		return path;
	}////////////////////////////
	
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

	public List<Path> twoHop(Author source, Entity sink, List<Entity> rids) {
		List<Path> paths = new LinkedList<Path>();
		
		List<Long> midIds = new LinkedList<Long>();
		for (Entity entity : source.getPapers()) {
			midIds.add(entity.getId());
		}
		List<Long> sinkRIds = new LinkedList<Long>();
		for (Entity entity : rids) {
			sinkRIds.add(entity.getId());
		}
		
		form2HopPaths(source.getAuId(), getIntersection( midIds,sinkRIds), sink.getId(), paths);
		
		return paths;
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
	
	public List<Path> threeHop(Author source, Entity sink, Set<AuIdandAfId> sinkAuthors, List<Entity> sinkRids) {
		List<Path> paths = new LinkedList<Path>();
		long sourceId = source.getAuId(), sinkId = sink.getId();
		Set<Long> midIds = toSet(sinkRids);
		for (Entity entityMid : source.getPapers()) {///作者的所有论文
			List<Path> twohops = twoHop(entityMid, sink, midIds);
			for (Path path : twohops) {
				path.pushNode(sourceId);
				paths.add(path);
			}
		}
		for(Long auId : sink.getAuId()) {
			Set<Long> afIds = new HashSet<>();
			for(AuIdandAfId sAuthor : sinkAuthors) {
				if(sAuthor.getAuId()==auId)
					afIds.add(sAuthor.getAfId());
			}
			form3HopPath(sourceId, getIntersection(source.getAfId(), new LinkedList<Long>(afIds)), auId, sinkId, paths);
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
		source = new Author();
		List<Entity> sinkRids = Collections.synchronizedList(new LinkedList<Entity>());
		Set<AuIdandAfId> sinkAuthors = new HashSet<>();
		CountDownLatch latch = new CountDownLatch(1);
		new AuthorThread(latch, source, sourceId).start();
		await(latch);
		
		latch = new CountDownLatch(2);
		new AllRIdThread(latch, sinkRids, sink).start();
		new SetThread(latch, sink.getAuId(), source.getAfId(), sinkAuthors).start();
		await(latch);
		
		Path paths1 = oneHop(source, sink);
		List<Path> paths2 = twoHop(source, sink, sinkRids);
		List<Path> paths3 = threeHop(source, sink, sinkAuthors, sinkRids);
		JSONArray array = new JSONArray();
		if (paths1 != null) array.add(paths1.getPath());
		for (Path path : paths2) array.add(path.getPath());
		for (Path path : paths3) array.add(path.getPath());
		return array.toString();
	}
	
	// id1=621499171&id2=2100837269
	public static void main(String[] args) {
		long AuId = 621499171;//2094437628L;//
		long Id = 2100837269;//273736245L;//
		Timer.init();
		AuId2IdSolver solver = new AuId2IdSolver();
		Author source = new Author();
		Entity sink = new Entity();
		CountDownLatch latch = new CountDownLatch(2);
		new AuthorThread(latch, source, AuId).start();
		new EntityThread(latch, sink, Id).start();
		await(latch);
		Timer.log();
		
		System.out.println(sink.getCC());
		List<Entity> sinkRids = Collections.synchronizedList(new LinkedList<Entity>());
		Set<AuIdandAfId> sinkAuthors = new HashSet<>(); 
		latch = new CountDownLatch(2);
		new AllRIdThread(latch, sinkRids, sink).start();
		new SetThread(latch, sink.getAuId(), source.getAfId(), sinkAuthors).start();
		await(latch);
		Timer.log();
		Path paths1 = solver.oneHop(source, sink);
		Timer.log();
		List<Path> paths2 = solver.twoHop(source, sink, sinkRids);
		List<Path> paths3 = solver.threeHop(source, sink, sinkAuthors, sinkRids);
		Timer.log();
		System.out.println("size=" + paths1);
		System.out.println("size=" + paths2.size());
		System.out.println("size=" + paths3.size());
		for (Path path : paths2) {
			System.out.println(path.getPath());
		}
		for (Path path : paths3) {
			System.out.println(path.getPath());
		}
	}
}
