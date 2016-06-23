import lombok.extern.java.Log;

import sx.blah.discord.api.*;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.MissingPermissionsException;

/**
 * Created author cade-home on 6/21/16.
 */
@Log
public class MouseBot {

    private static String BOT_TOKEN = "MTk0ODUyNDQxMTMxMzE5Mjk3.CkusdA.kawvMg6ptxG92c4Fnvq4651vWgA";

    public static MouseBot mouseBot;
    public IDiscordClient discordClient;

    private IChannel channel;
    private CommandListener commandListener;

    public static void main (String[] args) {
        mouseBot = new MouseBot();

    }

    public MouseBot() {
        try {
            discordClient = new ClientBuilder().withToken(BOT_TOKEN).login();
        } catch (DiscordException e) {
            log.warning("Constructor exception");
            return;
        }

        this.channel = discordClient.getChannelByID("\\#00");

        //register the CommandListener once the discord client is ready
        discordClient.getDispatcher().registerListener(new IListener<ReadyEvent>() {
            public void handle(ReadyEvent event) {
                discordClient.getDispatcher().registerListener(new CommandListener());
            }
        });
    }

    public void sayHello() throws MissingPermissionsException, HTTP429Exception, DiscordException {
        channel.sendMessage("Helloo!");
    }

    private class CommandListener {
        final static String KEY = "~";

        public CommandListener() {

        }

        /**
         * This is the first stop for processing all messages in the channel
         *
         * @param event
         */
        @EventSubscriber
        public void watchForCommands (MessageReceivedEvent event) {
            IMessage message = event.getMessage();
            String content = message.getContent();

            if(!content.startsWith(KEY))
                return;

            String command = content.toLowerCase();
            String[] args = null;

            if(content.contains(" ")) {
                command = command.split(" ")[0];
                args = content.substring(content.indexOf(' ') + 1).split(" ");
            }

            //dispatch a new event for this class to handle
            discordClient.getDispatcher().dispatch(new CommandExecutionEvent(message, command, message.getAuthor(), args));
        }

        @EventSubscriber
        public void ping(CommandExecutionEvent event) {
            if (event.isCommand("ping")) {
                try {
                    event.getMessage().reply("pong");
                } catch (MissingPermissionsException | HTTP429Exception | DiscordException e) {

                }
            }

        }
    }

    private class  CommandExecutionEvent extends Event {
        private final IMessage message;
        private final String command;
        private final IUser author;
        private final String[] args;

        public CommandExecutionEvent(IMessage message, String command, IUser author, String[] args)
        {
            this.message = message;
            this.command = command;
            this.author = author;
            this.args = args;
        }

        public String[] getArgs()
        {
            return args;
        }

        public IMessage getMessage()
        {
            return message;
        }

        public boolean isCommand(String command)
        {
            return command.equalsIgnoreCase(command);
        }

        public IUser getAuthor()
        {
            return author;
        }
    }


}

/* Event Examples


    private static class EventHandler {
        @EventSubscriber
        public void onReadyEvent(ReadyEvent event) {
            System.out.println("The bot is now ready");
            doSomething();
        }

        @EventSubscriber
        public void onMessageEvent(MessageReceivedEvent event) {
            System.out.println(event.getMessage().getAuthor().getName() + ": " + event.getMessage().getContent());
        }
    }

    private static class ReadyEventListener implements IListener<ReadyEvent> {
        public void handle(ReadyEvent event) {
            System.out.println("bot ready");
            doSomething();
        }
    }

     */
