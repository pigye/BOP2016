package solver;

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
import thread.AllIdThread;
import thread.AuthorThread;
import thread.EntityThread;
import thread.SetThread;

public class Id2AuIdSolver {
    private Entity source;
    private Author sink;
    
    public Id2AuIdSolver(){}
    
    public Id2AuIdSolver(Entity source, Author sink){
        this.source = source;
        this.sink = sink;
    }
    
    public Id2AuIdSolver(Entity source){
        this.source = source;
    }

	public Path oneHop(){
        Path path = null;
        Set<Long> set = new HashSet<>(source.getAuId());
        if (set.contains(sink.getAuId())){
            path = new Path(source.getId(), sink.getAuId());
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

    private void form2HopPaths(long source, List<Long> mid, long sink, List<Path> paths) {
        for (long item : mid) {
            paths.add(new Path(source, item, sink));
        }
    }
    
    public List<Path> twoHop(Entity source, long sinkId, Set<Long> set) {
        List<Path> paths = new LinkedList<Path>();
        form2HopPaths(source.getId(), getIntersection(source.getRId(), set), sinkId, paths);
        
        return paths;
    }
    public List<Path> twoHop(Entity source, Author sink, List<Entity> papers) {
        List<Path> paths = new LinkedList<Path>();
        form2HopPaths(source.getId(), getIntersection(source.getRId(), sink.getPapersId()), sink.getAuId(), paths);
        
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
    
    public List<Path> threeHop(Entity source, Author sink, Set<AuIdandAfId> afidandaudi, List<Entity> sourceRids, List<Entity> sinkids) {
        List<Path> paths = new LinkedList<Path>();
        long sourceId = source.getId(), sinkId = sink.getAuId();
        long cId = source.getCId(), jId = source.getJId();
        
        for (Entity item : sinkids) {
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
        Set<Long> set = new HashSet<>(sink.getPapersId());
        for (Entity rid : sourceRids) {
            List<Path> twohops = twoHop(rid, sink.getAuId(), set);
            for (Path path : twohops) {
                path.pushNode(sourceId);
                paths.add(path);
            }
        }
        
        // 列出从Id->AuId->AfId->AuId的路径
        for (AuIdandAfId id : afidandaudi){
            if (sink.getAfId().contains(id.getAfId())) {
            	paths.add(new Path(sourceId, id.getAuId(), id.getAfId(), sinkId));
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
    	sink = new Author();
    	Set<AuIdandAfId> afidandauid = new HashSet<>();
        List<Entity> sourceRids = new LinkedList<>();
    	CountDownLatch latch = new CountDownLatch(1);
    	new AuthorThread(latch, sink, sinkId).start();
    	await(latch);

    	latch = new CountDownLatch(2);
        new SetThread(latch, source.getAuId(), sink.getAfId(), afidandauid).start();
        new AllIdThread(latch, source.getRId(), sourceRids).start();
        await(latch);
        
        List<Path> paths = twoHop(source, sinkId, new HashSet<>(sink.getPapersId()));
        List<Path> paths2 = threeHop(source, sink, afidandauid, sourceRids, sink.getPapers());
        JSONArray array = new JSONArray();
		Path tmp = oneHop();
		if (tmp != null) array.add(tmp.getPath());
		for (Path path : paths) array.add(path.getPath());
		for (Path path : paths2) array.add(path.getPath());
		return array.toString();
    }
    //2094437628 2273736245l
    public static void main(String[] args) {
        Timer.init();
        Entity source = new Entity();
        Author sink = new Author();
        CountDownLatch latch = new CountDownLatch(2);
    	new EntityThread(latch, source, 2037920400).start();
    	new AuthorThread(latch, sink, 2105005017).start();
    	await(latch);
        Timer.log();
        Id2AuIdSolver solver = new Id2AuIdSolver(source, sink);
        Set<AuIdandAfId> afidandauid = new HashSet<>();
        List<Entity> sourceRids = new LinkedList<>();
        latch = new CountDownLatch(2);
        new SetThread(latch, source.getAuId(), sink.getAfId(), afidandauid).start();
        new AllIdThread(latch, source.getRId(), sourceRids).start();
        await(latch);
        Timer.log();
        List<Path> paths = solver.twoHop(source, sink.getAuId(), new HashSet<>(sink.getPapersId()));
        List<Path> paths2 = solver.threeHop(source, sink, afidandauid, sourceRids, sink.getPapers());
        Timer.log();
        Path tmp = solver.oneHop();
		if (tmp != null) System.out.println(tmp.getPath());
        System.out.println("size=" + paths.size());
        System.out.println("size=" + paths2.size());
        for (Path path : paths){
            System.out.println(path.getPath());
        }
        for (Path path : paths2) {
            System.out.println(path.getPath());
        }
    }
}
