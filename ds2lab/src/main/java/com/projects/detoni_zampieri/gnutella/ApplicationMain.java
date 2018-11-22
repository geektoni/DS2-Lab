package com.projects.detoni_zampieri.gnutella;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.List;

public class ApplicationMain {

    public static void main(String[] args) {

        final ActorSystem system = ActorSystem.create("adaptive-gossip");
        int numActors = 100;
        List<ActorRef> nodes = new ArrayList<ActorRef>();

        // Create the set of actors
        for (int i = 0; i < numActors; i++) {
            ActorRef a = system.actorOf(Props.create(GnutellaActor.class)
                    .withDispatcher("akka.actor.my-pinned-dispatcher"), "node-" + i);
            nodes.add(a);
        }

    }
}
