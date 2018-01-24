package akkameetup.demo.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akkameetup.demo.Main;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
public class MsgCollectorActor extends AbstractActor {

    @NonNull
    private int expectedMessageCount;
    @NonNull
    private int nrOfAsyncActors;
    @NonNull
    private int msgDelayInMs;
    @NonNull
    private int concurrentMessages;
    @NonNull
    private int nrOfBlockingActors;
    @NonNull
    private int logMessageModulo;

    private Date startDate;

    private List<ActorRef> refs = new ArrayList<>();

    private int receivedMessages = 0;

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static Props props(int expectedMessageCount, int nrOfAsyncActors, int msgDelayInMs, int concurrentMessages, int nrOfBlockingActors, int logMessageModulo) {
        return Props.create(MsgCollectorActor.class, expectedMessageCount, nrOfAsyncActors, msgDelayInMs, concurrentMessages, nrOfBlockingActors, logMessageModulo);
    }

    private AbstractActor.Receive busy;
    private void defineBusy() {
        busy = receiveBuilder()
                .match(String.class, s -> {
                     if(s.equals("GETREADY")) {
                        getContext().become(ready);
                    }
                })
                .build();
    }

    private AbstractActor.Receive ready;
    private void defineReady() {
        ready = receiveBuilder()
                .match(String.class, s -> {
                    if (s.equals("MSG")) {
                        ++receivedMessages;
                        if(receivedMessages % logMessageModulo == 0) {
                            log.info("received " + logMessageModulo + " messages");
                        }
                        if (receivedMessages >= expectedMessageCount) {
                            Date endDate = new Date();
                            int ms = (int)((endDate.getTime() - startDate.getTime()));
                            log.info("Received " + expectedMessageCount + " messages in " + ms + "ms!");
                            for (ActorRef ref : refs) {
                                getContext().getSystem().stop(ref);
                            }
                            getContext().become(busy);
                            Main.running = false;
                        }
                        else {
                            sender().tell("START", self());
                        }

                    } else if (s.equals("GETBUSY")) {
                        getContext().become(busy);
                    }
                })
                .build();
    }

    @Override
    public void preStart() {
        defineBusy();
        defineReady();
        for (int i = 0; i < nrOfAsyncActors; ++i) {
            refs.add(getContext().getSystem().actorOf(MsgActor.props(msgDelayInMs), "asyncMsgActor" + i));
        }

        for (int i = 0; i < nrOfBlockingActors; ++i) {
            refs.add(getContext().getSystem().actorOf(BlockingActor.props(msgDelayInMs), "blockMsgActor" + i));
        }
        getContext().become(ready);

        log.info("Expected: " + expectedMessageCount);
        log.info("Delay: " + msgDelayInMs);
        log.info("Actors: " + nrOfAsyncActors);
        log.info("Concurrent: " + concurrentMessages);
        log.info("Blocking: " + nrOfBlockingActors);
        startDate = new Date();
        refs.forEach(actorRef -> {
            for(int j = 0; j < concurrentMessages; ++j) {
                actorRef.tell("START", self());
            }
        });


    }

    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, s -> {
                    if(s.equals("GETREADY")) {
                        getContext().become(ready);
                    }
                    else if(s.equals("GETBUSY")) {
                        getContext().become(busy);
                    }
                })
                .build();
    }
}
