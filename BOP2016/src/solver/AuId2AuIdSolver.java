package solver;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import util.Prepare;
import util.Timer;
import bean.Author;
import bean.Entity;
import bean.Path;
import net.sf.json.JSONArray;
import thread.AuthorThread;

public class AuId2AuIdSolver {
    private Author source, sink;
    
    public AuId2AuIdSolver(){}
    
    public AuId2AuIdSolver(Author source, Author sink){
        this.source = source;
        this.sink = sink;
    }
    
    private List<Long> getIntersection(List<Long> x, List<Long> y) {
        Set<Long> set = new HashSet<>(y);
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
    
    public List<Path> twoHop(Author source, Author sink) {
        List<Path> paths = new LinkedList<Path>();
        form2HopPaths(source.getAuId(), getIntersection(source.getAfId(), sink.getAfId()), sink.getAuId(), paths);
        form2HopPaths(source.getAuId(), getIntersection(source.getPapersId(), sink.getPapersId()), sink.getAuId(), paths);        
        
        return paths;
    }
    public List<Path> twoHop(Entity id, Author sink, List<Entity> Rids) {
        List<Path> paths = new LinkedList<Path>();
        List<Long> midIds = new LinkedList<Long>();
        for (Entity entity : Rids) {
            midIds.add(entity.getId());
        }
        form2HopPaths(id.getId(), getIntersection(id.getRId(), midIds), sink.getAuId(), paths);

        return paths;
    }
    
    public List<Path> threeHop(Author source, Author sink, List<Entity> sourceIds, List<Entity> sinkRids) {
        List<Path> paths = new LinkedList<Path>();
        long sourceId = source.getAuId();

        for (Entity id : sourceIds) {
            List<Path> twohops = twoHop(id, sink, sinkRids);
            for (Path path : twohops) {
                path.pushNode(sourceId);
                paths.add(path);
            }
        }
        return paths;
    }
    
    public String getPath(long sourceId, long sinkId) {
    	source = new Author();
    	sink = new Author();
    	CountDownLatch latch = new CountDownLatch(2);
    	new AuthorThread(latch, source, sourceId).start();
    	new AuthorThread(latch, sink, sinkId).start();
    	try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        List<Path> paths = twoHop(source, sink);
        List<Path> paths2 = threeHop(source, sink, source.getPapers(), sink.getPapers());
        JSONArray array = new JSONArray();
        for (Path path : paths) array.add(path.getPath());
        for (Path path : paths2) array.add(path.getPath());
        return array.toString();
    }
    
    public static void main(String[] args) {
        Timer.init();
        AuId2AuIdSolver solver = new AuId2AuIdSolver();
        Author source = new Author();
        Author sink = new Author();
        CountDownLatch latch = new CountDownLatch(2);
    	new AuthorThread(latch, source, 57898110).start();
    	new AuthorThread(latch, sink, 2014261844).start();
    	try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        Timer.log();
        List<Path> paths = solver.twoHop(source, sink);
        Timer.log();
        List<Path> paths2 = solver.threeHop(source, sink, source.getPapers(), sink.getPapers());
        Timer.log();
        System.out.println("size=" + paths.size());
        System.out.println("size=" + paths2.size());
        for (Path path : paths) {
            System.out.println(path);
        }
    }
}
