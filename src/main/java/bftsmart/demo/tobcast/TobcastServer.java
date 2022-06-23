package bftsmart.demo.tobcast;


import bftsmart.communication.client.CommunicationSystemServerSide;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ReplicaContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.server.defaultservices.DefaultReplier;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;


public final class TobcastServer extends DefaultSingleRecoverable {
	private static final class TobcastReplier extends DefaultReplier {
		private ReplicaContext rc;

		@Override
		public void manageReply(TOMMessage request, MessageContext msgCtx) {
			CommunicationSystemServerSide com;
			int[] clients;

			if (request.reply.getContent().length == 0)
				return;

			try {
				synchronized (this) {
					while (rc == null)
						wait();
				}
			} catch (InterruptedException ex) {
				System.err.println(ex);
				return;
			}

			com = rc.getServerCommunicationSystem().getClientsConn();
			clients = com.getClients();
			com.send(clients, request.reply, false);
		}

		@Override
		public synchronized void setReplicaContext(ReplicaContext rc) {
			this.rc = rc;
			notifyAll();
		}
	}


	private final TobcastReplier replier = new TobcastReplier();


    public TobcastServer(int id) {
		new ServiceReplica(id, this, this, null, replier);
    }

    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
		return new byte[0];
    }

    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
		return command;
    }

    public static void main(String[] args){
        if(args.length < 1) {
            System.out.println("Use: java TobcastServer <processId>");
            System.exit(-1);
        }
        new TobcastServer(Integer.parseInt(args[0]));
    }

    @Override
    public void installSnapshot(byte[] state) {
    }

    @Override
    public byte[] getSnapshot() {
		return new byte[0];
    }
}
