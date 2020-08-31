package com.eriknj99.LANTrack.client;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    private static String HOSTNAME;
    private static int PORT;
    private static String UUID;
    private static String NAME;
    private static String IP;


    public static void main(String[] args){
        System.out.println("--- LANTrack Client v0.1 ---");

        Client c = new Client();
        System.out.println(c.toString());
        c.updateIP();
        System.out.println(c.getListing());
    }

}
