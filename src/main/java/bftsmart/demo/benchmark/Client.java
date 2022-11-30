package bftsmart.demo.benchmark;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.io.InputStreamReader;

import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.core.messages.TOMMessageType;
import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.RequestContext;
import java.util.logging.Level;


public class Client {
	private static final long[] latencies = new long[] {
		100, 500, 1000, 5000, 10000, 30000, Long.MAX_VALUE
	};
	private static final int[] latencyCounts = new int[latencies.length];

	private static long latencySum = 0;

	private static long latencyCount = 0;

	private static boolean firstReport = true;

	private static long lastReport;

	private static long start;

	private static byte[] tx;

	private static int done;


	private static class StatReplyListener implements ReplyListener {
		final long submitTime;

		long ackTime;


		StatReplyListener() {
			submitTime = System.currentTimeMillis();
			ackTime = 0;
		}


		@Override
		public void reset() {
		}

		@Override
		public void replyReceived(RequestContext ctx, TOMMessage reply) {
			long lat;
			int i;

			if (ackTime != 0)
				return;

			ackTime = System.currentTimeMillis();
			lat = ackTime - submitTime;

			latencySum += lat;
			latencyCount += 1;

			for (i = 0; i < latencies.length; i++)
				if (lat < latencies[i])
					break;

			if (i >= latencyCounts.length) {
				System.err.println("ERROR: impossible latency " + lat);
				return;
			}

			latencyCounts[i] += 1;
		}
	}

	private static void report() {
		long now = System.currentTimeMillis();
		int i, avglat;

		if (now < (lastReport + 1000))
			return;

		if (firstReport) {
			System.out.printf("%13s %9s %13s %9s %9s", "time", "#tx", "#B",
							  "ack", "avg lat");
			for (i = 0; i < latencies.length; i++) {
				if (latencies[i] == Long.MAX_VALUE)
					System.out.printf("     < inf");
				else
					System.out.printf(" %9s", "< " + latencies[i] + "ms");
			}
			System.out.printf("\n");
			System.out.printf("------------- --------- ------------- " +
							  "--------- ---------");
			for (i = 0; i < latencies.length; i++)
				System.out.printf(" ---------");
			System.out.printf("\n");

			firstReport = false;
		}

		if (latencyCount > 0)
			avglat = (int) (latencySum / latencyCount);
		else
			avglat = 0;

		System.out.printf("%13d %9d %13d %9d %9s", (now - start), done,
						  done * tx.length, latencyCount, avglat);

		for (i = 0; i < latencyCounts.length; i++)
			System.out.printf(" %9d", latencyCounts[i]);
		System.out.printf("\n");

		lastReport = now;
	}

    public static void main(String[] args) throws IOException {
		StatReplyListener listener;
		AsynchServiceProxy proxy;
		long deadline, now;
        int i, tps, allowed;

        if (args.length < 4) {
            System.out.println("Usage: java benchmark.Client <process-id> " +
							   "<tx-size> <duration> <tx-per-second>");
            System.exit(-1);
        }

		deadline = System.currentTimeMillis() +
			Integer.parseInt(args[2]) * 1000;

		tx = new byte[Integer.parseInt(args[1])];
		tps = Integer.parseInt(args[3]);

		System.out.println("Deadline = " + deadline);
		System.out.println("Tx Size  = " + tx.length);
		System.out.println("Tx Rate  = " + tps);

		proxy = new AsynchServiceProxy(Integer.parseInt(args[0]));

        try {
			start = System.currentTimeMillis();
			done = 0;

			while ((now = System.currentTimeMillis()) < deadline) {
				allowed = (int) (((now - start) * tps) / 1000);

				if (done >= allowed) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						break;
					}

					continue;
				}

				listener = new StatReplyListener();
				proxy.invokeAsynchRequest
					(tx, listener, TOMMessageType.ORDERED_REQUEST);
				done += 1;

				report();
			}

        } catch(Exception e){
            e.printStackTrace();
        } finally {
            proxy.close();
        }
    }
}
