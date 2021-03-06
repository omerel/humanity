package net.humanity_game.server.packets.core;

import com.google.common.base.Preconditions;
import net.humanity_game.server.packets.Packet;

import java.util.UUID;

public class Packet02Handshake extends Packet {

    private String name;

    public Packet02Handshake(UUID uuid, String name) {
        super(uuid);
        this.name = Preconditions.checkNotNull(name, "name");
    }

    public String getName() {
        return this.name;
    }

}
