/*
 * Made by Wannes 'W' De Smet
 * (c) 2011 Wannes De Smet
 * All rights reserved.
 * 
 */
package net.wgr.wcp.connectivity;

import java.io.IOException;
import java.util.UUID;
import net.wgr.server.messaging.ProtocolAgent;
import net.wgr.server.messaging.ProtocolHandler;
import net.wgr.wcp.Commander;
import net.wgr.wcp.command.Command;

/**
 * W Messaging Command Protocol
 * @created Jul 14, 2011
 * @author double-u
 */
public class MessagingConnection extends Connection {

    protected WMCP handler;
    protected UUID client;

    public MessagingConnection() {
        handler = new WMCP();
        ProtocolAgent.addProtocolHandler(handler);
    }

    @Override
    public void sendMessage(String data) throws IOException {
        if (client == null) {
            return;
        }
        handler.sendMessage(data, client.toString());
    }

    protected class WMCP extends ProtocolHandler {

        @Override
        public void handleMessageInChannel(String message, UUID client) {
            // Needs some fixing
            if (!MessagingConnection.this.client.equals(client)) {
                MessagingConnection.this.client = client;
                Commander.get().addConnection(MessagingConnection.this);
            }
            handleCommand(Command.parse(message, MessagingConnection.this));
        }

        @Override
        public String getQueueName() {
            return "WMCP";
        }
    }
}
