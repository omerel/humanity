package com.ttaylorr.dev.humanity.client;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.ttaylorr.dev.humanity.client.client.HumanityClient;
import com.ttaylorr.dev.humanity.server.handlers.Handler;
import com.ttaylorr.dev.humanity.server.handlers.HandlerSnapshot;
import com.ttaylorr.dev.humanity.server.handlers.Listenable;
import com.ttaylorr.dev.humanity.server.packets.Packet;
import com.ttaylorr.dev.humanity.server.packets.core.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ClientPacketHandler {

    private Map<Class<? extends Packet>, List<HandlerSnapshot>> handlers;
    private HumanityClient client;

    public ClientPacketHandler(HumanityClient client) {
        this.handlers = new HashMap<>();
        this.client = Preconditions.checkNotNull(client, "client");

        this.allowPackets();
    }

    private void allowPackets() {
        this.handlers.put(Packet01KeepAlive.class, new ArrayList<HandlerSnapshot>());
        this.handlers.put(Packet03Disconnect.class, new ArrayList<HandlerSnapshot>());
        this.handlers.put(Packet04Join.class, new ArrayList<HandlerSnapshot>());
        this.handlers.put(Packet05PlayerStateChange.class, new ArrayList<HandlerSnapshot>());
        this.handlers.put(Packet06HandUpdate.class, new ArrayList<HandlerSnapshot>());
        this.handlers.put(Packet07CreatePool.class, new ArrayList<HandlerSnapshot>());
        this.handlers.put(Packet08GameChangeState.class, new ArrayList<HandlerSnapshot>());
        this.handlers.put(Packet09MaskedJoin.class, new ArrayList<HandlerSnapshot>());
        this.handlers.put(Packet10PlayerUpdate.class, new ArrayList<HandlerSnapshot>());
        this.handlers.put(Packet11MaskedDisconnect.class, new ArrayList<HandlerSnapshot>());
    }

    public void handlePacket(Packet packet) {
        for (HandlerSnapshot handler : this.handlers.get(packet.getClass())) { // TODO sorting
            if (handler.getHandlingType().equals(packet.getClass())) {
                try {
                    this.client.getLogger().debug("(S->C) received: {}", packet.getClass().getSimpleName());
                    handler.getMethod().invoke(handler.getInstance(), packet);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void registerHandlers(Listenable listenable) {
        for (Method method : listenable.getClass().getMethods()) {
            if (method.getAnnotation(Handler.class) != null) {
                Handler annotation = method.getAnnotation(Handler.class);
                if (method.getParameterTypes().length == 1) {
                    Class<?> clazz = method.getParameterTypes()[0];
                    if (Packet.class.isAssignableFrom(clazz)) {
                        HandlerSnapshot snapshot = new HandlerSnapshot(listenable, method, (Class<? extends Packet>) clazz, annotation);
                        this.registerPacketHandler(snapshot, snapshot.getHandlingType());
                    } else {
                        throw new IllegalArgumentException("The first argument is not a packet");
                    }
                } else {
                    throw new IllegalArgumentException("No extra parameters may be provided other then the packet type");
                }
            }
        }
    }

    public boolean unregisterHandlers(Listenable listenable) {
        boolean removed = false;

        for (Map.Entry<Class<? extends Packet>, List<HandlerSnapshot>> entry : new HashSet<>(this.handlers.entrySet())) {
            for(HandlerSnapshot handler : new ArrayList<>(entry.getValue())) {
                if(handler.getInstance() == listenable) {
                    this.handlers.get(entry.getKey()).remove(handler);
                    removed = true;
                }
            }
        }

        if (removed) {
            this.client.getLogger().debug("Removed all handlers: " + listenable.getClass().getSimpleName());
        }
        return removed;
    }

    private boolean registerPacketHandler(HandlerSnapshot snapshot, Class<? extends Packet> handlingType) {
        if (!this.handlers.containsKey(handlingType)) {
            throw new IllegalArgumentException("cannot handle this type of packet");
        } else {
            List<HandlerSnapshot> handlers = this.handlers.get(handlingType);

            this.client.getLogger().debug("Registered handler {}.{}", snapshot.getInstance().getClass().getSimpleName(), snapshot.getMethod().getName());
            return handlers.add(snapshot);
        }
    }

    public List<HandlerSnapshot> getHandler(Class<? extends Packet> packet) {
        return ImmutableList.copyOf(this.handlers.get(packet));
    }

}
