package thread;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import bean.Entity;
import util.Prepare;

public class AllIdThread extends Thread{
	private CountDownLatch latch;
	private List<Entity> out;
	private List<Long> in;
	public AllIdThread(CountDownLatch latch, List<Long> in, List<Entity> out) {
		this.latch = latch;
		this.in = in;
		this.out = out;
	}
	
	@Override
	public void run() {
		try {
			new Prepare().selectAsId(in, Prepare.ALL_IDS, out);
		} finally {
			latch.countDown();
		}
	}
}
