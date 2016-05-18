package thread;

import java.util.concurrent.CountDownLatch;

import bean.Author;
import util.Prepare;

public class AuthorThread extends Thread{
	private CountDownLatch latch;
	private Author entity;
	private long id;
	public AuthorThread(CountDownLatch latch, Author entity, long id) {
		this.latch = latch;
		this.entity = entity;
		this.id = id;
	}
	@Override
	public void run() {
		try {
			Prepare prepare = new Prepare();
			prepare.selectAsAuthor(id, Prepare.ALL_IDS2, entity);
		} finally {
			latch.countDown();
		}
	}
}
