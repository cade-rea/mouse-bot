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
    private final static String KEY = "~";
    public static MouseBot mouseBot;

    private IDiscordClient discordClient;
    private IChannel channel;

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

        //register mouseBot as a listener once the discord client is ready
        discordClient.getDispatcher().registerListener(new IListener<ReadyEvent>() {
            public void handle(ReadyEvent event) {
                sendToChannel("mouse-bot ready");
                discordClient.getDispatcher().registerListener(mouseBot);
            }
        });
    }

    /**
     * Submits the message to the channel.
     * This is the last stop for processing messages.
     *
     * @param message
     */
    private void sendToChannel(String message) {
        if (channel != null){
            try {
                channel.sendMessage(message);
            } catch (MissingPermissionsException | HTTP429Exception | DiscordException e) {
                log.warning("sendToChannel() error: " + e.getMessage());
            }
        } else {
            log.info(message);
        }

    }

    /**
     * Monitors all messages for commands.
     * This is the first stop for processing all messages in the channel
     *
     * @param event
     */
    @EventSubscriber
    public void watchForCommands (MessageReceivedEvent event) {
        IMessage message = event.getMessage();
        String content = message.getContent();

        if (!content.startsWith(KEY))
            return;

        dispatchCommandEvent(message, content);
    }

    private void dispatchCommandEvent(IMessage message, String content) {
        //parse arguments from command
        String command = content.toLowerCase();
        String[] args = null;

        if(content.contains(" ")) {
            command = command.split(" ")[0];
            args = content.substring(content.indexOf(' ') + 1).split(" ");
        }

        //dispatch a new event for this class to handle
        discordClient.getDispatcher().dispatch(new CommandEvent(message, command, args));
    }

    @EventSubscriber
    public void ping(CommandEvent event) {
        if (event.isCommand("ping"))
            sendToChannel("pong");
    }

    private class CommandEvent extends Event {
        private final IMessage message;
        private final String command;
        private final IUser author;
        private final String[] args;

        public CommandEvent(IMessage message, String command, String[] args)
        {
            this.message = message;
            this.command = command;
            this.author = message.getAuthor();
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
            return this.command.equalsIgnoreCase(command);
        }

        public IUser getAuthor()
        {
            return author;
        }
    }
}
