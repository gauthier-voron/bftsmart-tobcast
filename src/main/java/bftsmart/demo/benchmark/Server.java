package bftsmart.demo.benchmark;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultRecoverable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public final class Server extends DefaultRecoverable  {
    private final ServiceReplica replica;
	private int requestCount = 0;
	private long lastReport = 0;
	private long byteCount = 0;


    public Server(int id) {
		replica = new ServiceReplica(id, this, this);
    }


	@Override
    public byte[][] appExecuteBatch(byte[][] cmds, MessageContext[] msgCtxs,
									boolean fromConsensus) {
        byte [][] replies;
		int i;

		replies = new byte[cmds.length][];

        for (i = 0; i < cmds.length; i++) {
            if (msgCtxs != null && msgCtxs[i] != null) {
				replies[i] = executeSingle(cmds[i], msgCtxs[i]);
            } else {
				replies[i] = executeSingle(cmds[i], null);
			}
        }

        return replies;
    }

    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
		return new byte[0];
    }

    private byte[] executeSingle(byte[] command, MessageContext msgCtx) {
		long now = System.currentTimeMillis();

		requestCount += 1;
		byteCount += command.length;

		if (now >= (lastReport + 1000)) {
			System.out.println(now + ": " + requestCount + " tx (= " +
							   byteCount + " B)");
			lastReport = now;
		}

		return new byte[0];
    }

    public static void main(String[] args){
        if(args.length < 1) {
            System.out.println("Use: java benchmark.Server <process-id>");
            System.exit(-1);
        }

		System.out.println(System.currentTimeMillis() + ": start");

        new Server(Integer.parseInt(args[0]));
    }


	@SuppressWarnings("unchecked")
	@Override
	public void installSnapshot(byte[] state) {
		System.out.println("WARNING: unwanted snapshot");
	}

	@Override
	public byte[] getSnapshot() {
		System.out.println("WARNING: unwanted snapshot");
		return new byte[0];
	}
}
