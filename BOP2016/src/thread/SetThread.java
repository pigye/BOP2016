package thread;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import bean.AuIdandAfId;
import util.Prepare;

public class SetThread extends Thread{
	private CountDownLatch latch;
	private List<Long> auids;
	private List<Long> afids;
	private Set<AuIdandAfId> set;
	public SetThread(CountDownLatch latch, List<Long> auids, List<Long> afids, Set<AuIdandAfId> set) {
		this.latch = latch;
		this.auids = auids;
		this.afids = afids;
		this.set = set;
	}
	
	@Override
	public void run() {
		try {
			new Prepare().selectAuId2AfId(auids, afids, "AA.AuId,AA.AfId", set);
		} finally {
			latch.countDown();
		}
	}
}
