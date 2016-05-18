package util;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import API.Evaluate;
import bean.AuIdandAfId;
import bean.Author;
import bean.Entity;
import thread.RIdThread;

public class Prepare {
	public static final int NUM = 50;
	public static final int POOL_SIZE = 5;
	
	public static final String ALL_IDS = "Id,CC,F.FId,C.CId,J.JId,AA.AuId,RId";
	public static final String ALL_IDS2 = "Id,F.FId,C.CId,J.JId,AA.AuId,AA.AfId,RId";
	public static final String NO_RID = "Id,CC,F.FId,C.CId,J.JId,AA.AuId";
	
	public Evaluate evaluate = new Evaluate();
	public JsonUtil util = new JsonUtil();
	
	public void selectAsRId(long rid, String attrs, long cc, List<Entity> list) {
		CountDownLatch latch = new CountDownLatch((int)Math.ceil(1.0 * cc / Evaluate.COUNT_INT));
		for (int offset = 0;cc > offset;offset += Evaluate.COUNT_INT) {
			new Thread(new RIdThread(list, "RId=" + rid, attrs, offset, latch)).start();
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void selectAsId(long id, String attrs, Entity entity) {
		String result = evaluate.evaluate("Id=" + id, 0, attrs);
		JsonNode array = util.toJson(result);
		util.getEntity(array.get(0), entity);
	}
	
	public void selectAsAuId(List<Long> auids, String attrs, Set<AuIdandAfId> res) {
		List <AuIdandAfId> list = new LinkedList<>();
		StringBuffer expr = new StringBuffer();
		Set<Long> set = new HashSet<>(auids);
		for (int i = 0;i < auids.size();i += NUM) {
			int limit = Math.min(NUM, auids.size() - i);
			expr.append("Composite(");
			for (int j = 0;j < limit - 1;j ++) expr.append("Or(");
	        expr.append("AA.AuId=" + auids.get(i));
	        for (int j = 1;j < limit;j ++) {
	            expr.append(",AA.AuId=" + auids.get(i + j) + ")");
	        }
	        expr.append(")");
			String result = evaluate.evaluate(expr.toString(), 100000, 0, attrs);
			JsonNode array = util.toJson(result);
			for (int j = 0;j < array.size();j ++) {
				list.addAll(util.getAfIdsandAuIds(array.get(j)));
			}
			for (AuIdandAfId a : list) {
				if (set.contains(a.getAuId())) res.add(a);
			}
		}
	}
	
	//Composite(And(Or(AA.AuId=2099495348,AA.AuId=676500258),Or(AA.AfId=63966007,AA.AfId=188538660)))
	public void selectAuId2AfId(List<Long> auids, List<Long> afids, String attrs, Set<AuIdandAfId> res) {
		if (auids == null || auids.size() == 0 || afids == null || afids.size() == 0) return ;
		StringBuffer expr = new StringBuffer();
		Set<Long> set = new HashSet<>(auids);
		expr.append("Composite(");
		if((auids != null || auids.size() != 0)
				&& (afids != null || afids.size() != 0)){
			expr.append("And(");
		}
		for (int j = 0;j < auids.size() - 1;j ++) expr.append("Or(");
		expr.append("AA.AuId=" + auids.get(0));
		for (int j = 1;j < auids.size();j ++) {
			expr.append(",AA.AuId=" + auids.get(j) + ")");
		}
		if((auids != null || auids.size() != 0)
				&& (afids != null || afids.size() != 0)){
			expr.append(",");
		}
		for (int j = 0;j < afids.size() - 1;j ++) expr.append("Or(");
		expr.append("AA.AfId=" + afids.get(0));
		for (int j = 1;j < afids.size();j ++) {
			expr.append(",AA.AfId=" + afids.get(j) + ")");
		}
		if((auids != null || auids.size() != 0)
				&& (afids != null || afids.size() != 0)){
			expr.append(")");
		}
		expr.append(")");
		//cause bug in competition 
		if (expr.toString().length() > 1600) {
			selectAsAuId(auids, attrs, res);
		} else {
			String result = evaluate.evaluate(expr.toString(), 100000, 0, attrs);
			JsonNode array = util.toJson(result);
			for (int j = 0;j < array.size();j ++) {
				res.addAll(util.getAfIdsandAuIds(array.get(j), auids, afids));
			}
		}
	}

	public void selectAsAuthor(long AuId, String attrs, Author author){
	    String result = evaluate.evaluate("Composite(AA.AuId=" + AuId + ")", 0, attrs);
	    JsonNode array = util.toJson(result);
	    util.getAuthor(AuId, array, author);
	}
	
	public void selectAsId(List<Long> ids, String attrs, List<Entity> entities) {
		for (int i = 0;i < ids.size();i += NUM) {
			StringBuffer expr = new StringBuffer();
			int limit = Math.min(NUM, ids.size() - i);
			for (int j = 0;j < limit - 1;j ++) expr.append("Or(");
			expr.append("Id=" + ids.get(i));
			for (int j = 1;j < limit;j ++) {
				expr.append(",Id=" + ids.get(i + j) + ")");
			}
			String result = evaluate.evaluate(expr.toString(), 0, attrs);
			JsonNode array = util.toJson(result);
			for (int j = 0;j < array.size();j ++) {
				Entity entity = new Entity();
				util.getEntity(array.get(j), entity);
				entities.add(entity);
			}
		}
	}
	
	public static void main(String[] args) {
		Timer.init();
		long st = System.currentTimeMillis();
		testAsRId();
		System.out.println((System.currentTimeMillis() - st) / 1000.0);
	}
	
	public static void testAsRId() {
		//2147152072 2292217923
		Prepare prepare = new Prepare();
		List<Entity> list = Collections.synchronizedList(new LinkedList<Entity>());
		prepare.selectAsRId(2292217923l, "Id,F.FId,C.CId,J.JId,AA.AuId", 150000, list);
		System.out.println(list.size());
	}
}
