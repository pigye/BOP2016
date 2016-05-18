package thread;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import bean.Entity;
import util.Prepare;

public class AllRIdThread extends Thread{
	private CountDownLatch latch;
	private List<Entity> entities;
	private Entity entity;
	public AllRIdThread(CountDownLatch latch, List<Entity> entities, Entity entity) {
		this.latch = latch;
		this.entities = entities;
		this.entity = entity;
	}
	
	@Override
	public void run() {
		try {
			new Prepare().selectAsRId(entity.getId(), Prepare.NO_RID, entity.getCC(), entities);
		} finally {
			latch.countDown();
		}
	}
}
