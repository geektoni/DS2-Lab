package com.projects.detoni_zampieri.gnutella;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.projects.detoni_zampieri.consensus.messages.PingMessage;
import scala.concurrent.duration.Duration;

public class GnutellaActor extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	public static Props props() {
		return Props.create(GnutellaActor.class);
	}
	
	public static class TestStability {
		public boolean next = true;
		
		public TestStability(boolean next) {
			this.next = next;
		}
	}
	
	
    public static class StartMessage {
    	protected final ActorRef initPeer;
    	protected final int id;
		
        public StartMessage(ActorRef ip, int id) {
            this.initPeer = ip;
            this.id = id;
        }
        
        public ActorRef getInitPeer() {
        	return initPeer;
        }
        
        public int getID() {
        	return id;
        }
    }
    
    public static class GnutellaPingMessage {
		GnutellaPingMessage(ActorRef init, int id, int ttl)
		{
			this.initiator = init;
			this.id = id;
			this.TTL = ttl;
		}

		public GnutellaPingMessage copy()
		{
			GnutellaPingMessage cp = new GnutellaPingMessage(this.initiator, this.id, this.TTL-1);
			return cp;
		}

		public ActorRef initiator;
		public int id;
		public int TTL;
    }
    
    public static class GnutellaPongMessage {

		GnutellaPongMessage(ActorRef init, int id)
		{
			this.replier = init;
			this.id = id;
		}

		public ActorRef replier;
		public int id;
    }
    
    protected void insertPeer(int pID, ActorRef peer) {
    	if (!peerMap.containsKey(pID)) {
    		peerMap.put(pID, peer);
    		log.info("node {} add {}", myID, pID);
    	}
    }
    
    protected Map<Integer, ActorRef> peerMap = new HashMap<>();
    protected int myID = 0;
    public static final int ITTL = 2;

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof StartMessage) {
			GnutellaPingMessage msg = new GnutellaPingMessage(getSelf(), this.myID, ITTL);
			((StartMessage)message).getInitPeer().tell(msg, getSelf());
		} else if (message instanceof GnutellaPingMessage) {

			GnutellaPingMessage msg = (GnutellaPingMessage) message;
			insertPeer(msg.id, msg.initiator);

			GnutellaPongMessage answer = new GnutellaPongMessage(getSelf(), this.myID);
			msg.initiator.tell(answer, getSelf());

			if (msg.TTL > 1)
			{
				for (ActorRef r : peerMap.values())
				{
					if (!r.equals(getSender()))
					{
						r.tell(msg.copy(), getSelf());
					}
				}
			}

		} else if (message instanceof GnutellaPongMessage) {

			GnutellaPongMessage msg = (GnutellaPongMessage) message;
			insertPeer(msg.id, msg.replier);

		} else if (message instanceof TestStability) {
			String peers = "";
			for (int p : peerMap.keySet()) {
				peers += " " + p;
			}
			log.info("node {} -> {}", myID, peers);
		} else {
            unhandled(message);
        }
	}
}
