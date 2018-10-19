package com.projects.detoni_zampieri.adaptiveGossip;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

import java.util.*;

public class GossipActor extends UntypedActor {

    public GossipActor() {

        // Initialize default variables
        this.rng = new Random();
        this.nodeId = rng.nextInt();

        this.events = new ArrayList<Event>();
        this.minBuffer = MAX_BUFFER_SIZE - this.events.size();
        this.k = 10;
        this.f = 3;

        this.delta = 2;
        this.s = 2;
        this.minBuffers = new ArrayList<Integer>();
        for (int i=0; i<delta; i++)
        {
            this.minBuffers.add(i, MAX_BUFFER_SIZE - this.events.size());
        }

    }

    @Override
    public void onReceive(Object o) throws Exception {

        // Catch the list message
        if (o instanceof ListMessage)
        {
            this.peers = ((ListMessage) o).m_nodes;
        } else if (o instanceof UpdateAgesAndGossipMessage)
        {
            // Increment age of events
            for (Event e : events)
            {
                e.incrementAge();
            }

            // Remove oldest elements
            this.events.removeIf(e -> e.age > k);

            // Add the newly generated event to the list
            this.events.add(((UpdateAgesAndGossipMessage)o).newEvent);

            // Send gossip to everybody
            gossipMulticast(new Message(new ArrayList<>(this.events),
                    this.s,
                    this.minBuffer));

        }else if (o instanceof EnterNewPeriodMessage){

            this.s++;
            this.minBuffers.add(s-1, this.MAX_BUFFER_SIZE-this.events.size());
            this.minBuffer = this.minBuffers.get(s-1);

            for(int i=s-1; i>s-1-delta-1; i--)
            {
                if (this.minBuffers.get(i) < this.minBuffer)
                {
                    this.minBuffer = this.minBuffers.get(i);
                }
            }


        } else if (o instanceof Message) {
            onReceiveGossip((Message)o);
        } else
        {
            unhandled(o);
        }
    }

    private void gossipMulticast(Message m) {
        // Select f random processes
        // and send them the message
        Collections.shuffle(this.peers);
        int skip = 0;
        for (int i = 0; i < f; i++) {
            // Do not send a message to myself (h@ck3r w@y)
            if (this.peers.get(i+skip).equals(getSelf())) {
                skip++;
                i--;
            } else {
                this.peers.get(i + skip).tell(m, ActorRef.noSender());
            }
        }
    }

    public Event getLocalEvent(Event e)
    {
        return this.events.get(this.events.indexOf(e));
    }

    public void onReceiveGossip(Message gossip)
    {
        //update my events
        for(Event e:gossip.events)
        {
            if(!this.events.contains(e))
            {
                this.events.add(e);
                deliver(e);
            }
            else {
                Event e_prime = getLocalEvent(e);
                if(e_prime.age < e.age)
                    e_prime.age = e.age;
            }
        }
        if(this.events.size() > this.MAX_BUFFER_SIZE)
        {
            //remove the excess
            ArrayList<Event> sorted_events = new ArrayList<>(this.events);
            Collections.sort(sorted_events,(e1,e2)-> e2.age - e1.age);
            int diff = this.events.size() - MAX_BUFFER_SIZE;
            Iterator<Event> iter = sorted_events.iterator();
            for(int i=0;i<diff;i++)
            {
                iter.next();
                iter.remove();
            }
            
        }

        // Update congestion rates
        if (gossip.age == this.s && gossip.minBuffer < this.minBuffers.get(s-1))
        {
            this.minBuffers.set(s-1, gossip.minBuffer);
        }

    }

    public void deliver(Event e)
    {
        System.out.println("Received event "+e.id.toString());
    }

    // Default variables for the actor
    public Random rng;
    public int nodeId;
    public List<ActorRef> peers;

    // Adaptive Gossip Variables
    private int MAX_BUFFER_SIZE = 100; // Max number of messages
    public List<Event> events; // Buffer for messages
    public int minBuffer; // Minimal size of the buffer
    public List<Integer> minBuffers;
    public int s; // Current period
    public int delta; // Interval for computing minBuffer
    public int k; // Maximum age for events
    public int f; // Total number of random peers (fanout)
}
