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
public class BlockingActor extends AbstractActor {

    @NonNull
    private int msgDelayInSec;

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static Props props(int msgDelayInSec) {
        return Props.create(BlockingActor.class, msgDelayInSec);
    }

    @Override
    public void preStart() {
    }

    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, s -> {
                    if(s.equals("START")) {
                        log.info("BLOCK");
                        TimeUnit.SECONDS.sleep(msgDelayInSec);
                        getSender().tell("MSG", self());
                    }
                })
                .build();
    }
}
