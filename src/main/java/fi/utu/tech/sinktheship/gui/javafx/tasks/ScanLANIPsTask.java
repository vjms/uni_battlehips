package fi.utu.tech.sinktheship.gui.javafx.tasks;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import fi.utu.tech.sinktheship.network.Server;
import fi.utu.tech.sinktheship.network.ServerInfo;
import fi.utu.tech.sinktheship.network.NetworkInfo;
import javafx.concurrent.Task;

public class ScanLANIPsTask extends Task<ArrayList<ServerInfo>> {

	private int timeout;
	private final int maxThreads = 32;

	public ScanLANIPsTask(int timeout) {
		this.timeout = timeout;
	}

	@Override
	protected ArrayList<ServerInfo> call() throws Exception {
		var ret = new ArrayList<ServerInfo>();
		var info = NetworkInfo.getNetworkInfo();
		var addrcount = info.addressCount();
		var masked = info.maskedAsNum();
		var threadpool = new Thread[Math.min(addrcount / 10, maxThreads)];
		AtomicInteger current = new AtomicInteger(-1);
		for (int i = 0; i < threadpool.length; i++) {
			threadpool[i] = new Thread(() -> {
				int next;
				while ((next = current.incrementAndGet()) < addrcount) {
					var ip = NetworkInfo.ipv4NumToString(masked + next);
					var serv = Server.scan(ip, Server.defaultPort, timeout);
					if (serv != null) {
						synchronized (ret) {
							ret.add(serv);
							updateValue(ret);
						}
					}
				}
			});
		}
		for (var thread : threadpool) {
			thread.start();
		}
		for (var thread : threadpool) {
			thread.join();
		}
		return ret;
	}

}
