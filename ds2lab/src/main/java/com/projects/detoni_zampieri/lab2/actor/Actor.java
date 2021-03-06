package com.projects.detoni_zampieri.lab2.actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.projects.detoni_zampieri.lab2.message.ActorListMessage;
import com.projects.detoni_zampieri.lab2.message.EpidemicValue;
import com.projects.detoni_zampieri.lab2.message.GenerateUpdate;
import com.projects.detoni_zampieri.lab2.message.Message;
import com.projects.detoni_zampieri.lab2.message.KillMessage;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Random;

public abstract class Actor extends UntypedActor {

    private ArrayList<ActorRef> peers;
    private Random rnd;
    protected EpidemicValue value;
    protected int round;
    protected long timeout;
    protected long delta;
    protected int actorId;
    protected boolean run;

    public Actor()
    {
        this.rnd = new Random();
        this.value = new EpidemicValue(new Timestamp(System.currentTimeMillis()), 1);
        this.round = 0;
        this.delta = 100;
        this.actorId = rnd.nextInt();
        this.run = true;
    }

    public void onReceive(Object message) throws Exception {
        if (message instanceof ActorListMessage)
        {
            onActorList((ActorListMessage) message);
        } else if(message instanceof GenerateUpdate){
            onGenerateUpdate((GenerateUpdate)message);
        } else if(message instanceof KillMessage){
        	onKillMessage((KillMessage)message);
        } else {
            unhandled(message);
        }
    }

    protected void onKillMessage(KillMessage message) { this.run = false; }
    
    protected void onGenerateUpdate(GenerateUpdate message) {
        this.value = new EpidemicValue(new Timestamp(System.currentTimeMillis()),rnd.nextInt());
    }

    protected void onActorList(ActorListMessage msg) {
    	this.peers = msg.nodes;
    	runSchedule();
    }

    protected void sendMessage(Message msg){

        int index=-1;
        do {
            index = rnd.nextInt(peers.size());
        } while (this.peers.get(index).equals(this.getSelf()));

        this.peers.get(index).tell(msg, this.getSelf());
    }

    protected void runSchedule() {
        Thread t = new Thread(new Runnable() {

            public void run() {
                // TODO Auto-generated method stub
                while (run) {
                    if (System.currentTimeMillis() >= timeout) {
                        onEpidemicTimeout();
                        round++;
                        setEpidemicTimeOut();
                    }
                }
            }
        });
        t.start();
    }

    protected void setEpidemicTimeOut()
    {
        timeout = System.currentTimeMillis() + delta;
    }

    protected abstract void onEpidemicTimeout();
    //{ System.out.println("Wrong method");}

    public static Props props() {
        //return Props.create(Actor.class,()->new Actor());
        return null;
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        System.out.println("Actor "+ String.valueOf(this.actorId)+", value: "+ String.valueOf(this.value.getValue()));
    }
}
