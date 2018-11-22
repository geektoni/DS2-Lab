package com.projects.detoni_zampieri.gnutella;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ApplicationMain {

    public static void main(String[] args) {

        final ActorSystem system = ActorSystem.create("gnutella");
        int numActors = 100;
        List<ActorRef> nodes = new ArrayList<ActorRef>();

        // Create the set of actors
        for (int i = 0; i < numActors; i++) {
            ActorRef a = system.actorOf(GnutellaActor.props(i)
                    .withDispatcher("akka.actor.my-pinned-dispatcher"), "node-" + i);
            nodes.add(a);
        }

        try {
	        Random rng = new Random();
	        
	        System.out.println(">>> Sending 'StartMessage' <<<");
	        
	        for(ActorRef node : nodes)
	        {
	        	int random_id = rng.nextInt(nodes.size());
	        	node.tell(new GnutellaActor.StartMessage(nodes.get(random_id), random_id), ActorRef.noSender());
	        }
	        
	        System.out.println(">>> Press ENTER to see system status <<<");
	        System.in.read();
	        
	        
	        for(ActorRef node : nodes)
	        {
	        	node.tell(new GnutellaActor.TestStability(true), ActorRef.noSender());
	        }
        
        	System.out.println(">>> Press ENTER to exit <<<");
        	System.in.read();
        } catch (IOException ioe) {
        	ioe.printStackTrace();
        } finally {
        	 system.terminate();
        }
        
    }
}
