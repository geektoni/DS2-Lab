package com.projects.detoni_zampieri.lab1;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.projects.detoni_zampieri.lab1.message.NodeListMessage;
import com.projects.detoni_zampieri.lab1.message.StartBroadcastMessage;

import java.util.ArrayList;

public class ApplicationMain{
    public static void main(String args[])
    {
        final ActorSystem system = ActorSystem.create("reliable_broadcast");
        int numActors = 4;
        ArrayList<ActorRef> nodes = new ArrayList<>();

        for (int i = 0; i < numActors; i++) {
            ActorRef a = system.actorOf(Props.create(Node.class).withDispatcher("akka.actor.my-pinned-dispatcher"), "node-"+i);
            nodes.add(a);
        }

        // let the nodes know who is in the group
        for(ActorRef a:nodes)
        {
            a.tell(new NodeListMessage(-1,nodes),null);
        }
        
        // tell the nodes to start sending messages
        for(ActorRef a:nodes)
        {
            a.tell(new StartBroadcastMessage(-1),null);
        }
    }
}