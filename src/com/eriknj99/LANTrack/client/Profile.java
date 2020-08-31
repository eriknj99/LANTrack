package com.eriknj99.LANTrack.client;

public class Profile {
    final String name;
    final String UUID;
    String IP;

    public Profile(String name, String UUID, String IP) {
        this.name = name;
        this.UUID = UUID;
        this.IP = IP;
    }
}
