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

			System.out.println(submitTime + " -> " + ackTime + " = " + lat);
		}
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
			}

        } catch(Exception e){
            e.printStackTrace();
        } finally {
            proxy.close();
        }
    }
}
