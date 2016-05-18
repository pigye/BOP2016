import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import API.Evaluate;
import bean.Entity;
import solver.AuId2AuIdSolver;
import solver.AuId2IdSolver;
import solver.Id2AuIdSolver;
import solver.Id2IdSolver;
import thread.EntityThread;
import util.Timer;

public class BOPServlet extends HttpServlet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	static {
		new Thread() {
			public void run() {
				Evaluate evaluate = new Evaluate();
				while (true) {
					System.out.println("Heart Beating!!!");
					evaluate.evaluate("Id=1983578042", 0, "Id");
					try {
						sleep(60 * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Timer.init();
		long id1 = Long.parseLong(req.getParameter("id1"));
		long id2 = Long.parseLong(req.getParameter("id2"));
		long st = System.currentTimeMillis();
		System.out.println(id1 + "&" + id2);
		String ret = solve(id1, id2);
		PrintWriter writer = resp.getWriter();
		writer.write(ret);
		writer.flush();
		writer.close();
		System.out.println((System.currentTimeMillis() - st) / 1000.0);
	}
	
	private String solve(long id1, long id2) {
		Entity a = new Entity(), b = new Entity();
		CountDownLatch latch = new CountDownLatch(2);
		new EntityThread(latch, a, id1).start();
		new EntityThread(latch, b, id2).start();
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		boolean flag1 = (a.getAuId() != null && !a.getAuId().isEmpty());
		boolean flag2 = (b.getAuId() != null && !b.getAuId().isEmpty());
		System.out.println(flag1 + "----" + flag2);
		String ret = null;
		if (flag1 && flag2) ret = new Id2IdSolver(a, b).getPath(id1, id2);
		else if (!flag1 && flag2) ret = new AuId2IdSolver(b).getPath(id1, id2);
		else if (flag1 && !flag2) ret = new Id2AuIdSolver(a).getPath(id1, id2);
		else ret = new AuId2AuIdSolver().getPath(id1, id2);
		return ret;
	}
}
