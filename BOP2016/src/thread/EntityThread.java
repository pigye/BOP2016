package thread;

import java.util.concurrent.CountDownLatch;

import com.fasterxml.jackson.databind.JsonNode;

import API.Evaluate;
import bean.Entity;
import util.JsonUtil;
import util.Prepare;

public class EntityThread extends Thread{
	private CountDownLatch latch;
	private Entity entity;
	private long id;

	public EntityThread(CountDownLatch latch, Entity entity, long id) {
		this.latch = latch;
		this.entity = entity;
		this.id = id;
	}

	@Override
	public void run() {
		try {
			Evaluate evaluate = new Evaluate();
			JsonUtil util = new JsonUtil();
			String result = evaluate.evaluate("Id=" + id, 0, Prepare.ALL_IDS);
			JsonNode array = util.toJson(result);
			JsonNode object = array.get(0);
			if (object != null) {
				util.getEntity(object, entity);
			}
		} finally {
			latch.countDown();
		}
	}
	
	public interface Query {
		public void query(Object t);
	}
}
