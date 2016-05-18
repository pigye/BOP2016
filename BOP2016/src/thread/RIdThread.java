package thread;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.fasterxml.jackson.databind.JsonNode;

import API.Evaluate;
import bean.Entity;
import util.JsonUtil;

public class RIdThread implements Runnable {
	protected List<Entity> list;
	protected String expr;
	protected String attrs;
	protected int offset;
	protected CountDownLatch cnt;

	public RIdThread(List<Entity> list, String expr, String attrs, int offset, CountDownLatch cnt) {
		this.list = list;
		this.expr = expr;
		this.attrs = attrs;
		this.offset = offset;
		this.cnt = cnt;
	}

	@Override
	public void run() {
		try {
			Evaluate evaluate = new Evaluate();
			JsonUtil util = new JsonUtil();
			String result = evaluate.evaluate(expr, offset, attrs);
			JsonNode array = util.toJson(result);
			List<Entity> entities = new LinkedList<>();
			for (int i = 0;i < array.size();i ++) {
				Entity entity = new Entity();
				util.getEntity(array.get(i), entity);
				entities.add(entity);
			}
			list.addAll(entities);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cnt.countDown();
		}
	}
}
