package akkameetup.demo;

import java.util.concurrent.TimeUnit;

import akka.actor.ActorSystem;
import akkameetup.demo.actor.MsgCollectorActor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

public class Main {

    public static boolean running = true;
    public static void main(String[] args) throws Exception {

        Options options = new Options();
        options.addOption("m", "nr-messages", true, "Number of messages that should be sent");
        options.addOption("a", "nr-async-actors", true, "Number of async msg actors that should be started");
        options.addOption("d", "delay", true, "msg delay in (in ms for async-actors and in seconds for blocking actors");
        options.addOption("c", "concurrent", true, "concurrent messages per actor");
        options.addOption("b", "nr-blocking-actors", true, "Number of blocking msg actors that should be started");
        options.addOption("l", "log-every", true, "Message log modulo");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        int messages = Integer.parseInt(cmd.getOptionValue("m"));
        int actors = Integer.parseInt(cmd.getOptionValue("a"));
        int delay = Integer.parseInt(cmd.getOptionValue("d"));
        int concurrentMessages = Integer.parseInt(cmd.getOptionValue("c"));
        int blocking = Integer.parseInt(cmd.getOptionValue("b"));
        int logModulu = Integer.parseInt(cmd.getOptionValue("l"));


        ActorSystem system = ActorSystem.create("akka-test");
        try {
            system.actorOf(MsgCollectorActor.props(messages, actors, delay, concurrentMessages, blocking, logModulu), "msgCollector");
            while (running) {
                System.out.println("THREADS: " + java.lang.Thread.activeCount());
                TimeUnit.SECONDS.sleep(1);

            }
        }
        finally {
            system.terminate();
        }
    }

}