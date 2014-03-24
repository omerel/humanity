package com.ttaylorr.dev.humanity.server;

import com.google.common.base.Preconditions;
import com.ttaylorr.dev.humanity.server.cards.BlackCardDeck;
import com.ttaylorr.dev.humanity.server.cards.WhiteCard;
import com.ttaylorr.dev.humanity.server.cards.WhiteCardDeck;
import com.ttaylorr.dev.humanity.server.cards.factory.BlackCardFactory;
import com.ttaylorr.dev.humanity.server.cards.factory.WhiteCardFactory;
import com.ttaylorr.dev.humanity.server.client.ClientManager;
import com.ttaylorr.dev.humanity.server.listeners.KeepAliveListener;
import com.ttaylorr.dev.humanity.server.packets.PacketHandler;
import com.ttaylorr.dev.humanity.server.listeners.HandshakeListener;
import com.ttaylorr.dev.humanity.server.queue.core.*;
import com.ttaylorr.dev.logger.Logger;
import com.ttaylorr.dev.logger.LoggerProvider;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

public class HumanityServer {

    private PacketHandler packetHandler;

    private ClientManager clientManager;
    private OutboundPacketQueue outboundPacketQueue;

    private ConnectionListener connectionListener;

    private ServerSocket serverSocket;
    private boolean open;

    private int port;

    private Logger logger;

    private WhiteCardDeck whiteCardDeck;
    private BlackCardDeck blackCardDeck;

    public HumanityServer(int port) {
        Preconditions.checkArgument(port > 0, "port must be greater than 0");
        this.logger = LoggerProvider.putLogger(this.getClass());
        this.port = port;
        this.packetHandler = new PacketHandler(this);
        this.clientManager = new ClientManager(this);
    }

    public void open() {
        this.logger.info("Opening server \"humanity\"...");
        try {
            this.setup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        this.logger.info("Closing server \"humanity\"...");
        try {
            this.teardown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setup() throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.open = true;

        this.clientManager.setup();
        this.outboundPacketQueue = new OutboundPacketQueue(this);
        Thread outboundPacketQueueThread = new Thread(this.outboundPacketQueue);
        outboundPacketQueueThread.setName("OutboundPacketQueue");
        outboundPacketQueueThread.start();

        this.registerHandlers();

        this.whiteCardDeck = new WhiteCardFactory(new File("./server/src/main/resources/cards-all.json"), this).parse().build();
        this.blackCardDeck = new BlackCardFactory(new File("./server/src/main/resources/cards-all.json"), this).parse().build();

        this.connectionListener = new ConnectionListener(this);
        Thread thread = new Thread(connectionListener);
        thread.setName("Connection-Listener");
        thread.start();
    }

    private void registerHandlers() {
        this.packetHandler.registerHandlers(new HandshakeListener(this));
        this.packetHandler.registerHandlers(new KeepAliveListener(this));
    }

    private void teardown() throws IOException {
        this.open = false;
        this.clientManager.disconnectAll(this);
        this.serverSocket.close();
    }

    public ServerSocket getSocket() {
        return this.serverSocket;
    }

    public ClientManager getClientManager() {
        return this.clientManager;
    }

    public PacketHandler getPacketManager() {
        return this.packetHandler;
    }

    public OutboundPacketQueue getOutboundPackets() {
        return this.outboundPacketQueue;
    }

    public boolean isOpen() {
        return this.open;
    }

    public Logger getLogger() {
        return this.logger;
    }
}
