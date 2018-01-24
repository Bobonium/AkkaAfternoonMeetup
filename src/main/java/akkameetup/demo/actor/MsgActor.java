package akkameetup.demo.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class MsgActor extends AbstractActor {

    @NonNull
    private int msgDelayInMs;

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static Props props(int msgDelayInMs) {
        return Props.create(MsgActor.class, msgDelayInMs);
    }

    @Override
    public void preStart() {
    }

    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, s -> {
                    if(s.equals("START")) {
                        if(msgDelayInMs == 0) {
                            getSender().tell("MSG", self());
                        }
                        else {
                            context().system().scheduler().scheduleOnce(
                                    Duration.create(msgDelayInMs, TimeUnit.MILLISECONDS),
                                    sender(),
                                    "MSG",
                                    context().system().dispatcher(),
                                    self()
                            );
                        }
                    }
                })
                .build();
    }
}
