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

        IP = getIP();

        String config = readConfig();

        if(config == null){
            System.out.println("---Setup Wizard---");
            Scanner sc = new Scanner(System.in);

            System.out.print("Enter Server hostname: ");
            HOSTNAME = sc.nextLine();

            System.out.print("\nEnter Server port: ");
            PORT = Integer.parseInt(sc.nextLine());

            System.out.print("\nEnter a name for this machine:");
            NAME = sc.nextLine();

            String regResponse = register(NAME);
            if(!regResponse.equals("ERROR")){
                UUID = regResponse;
            }

            String newConfig = "HOSTNAME " + HOSTNAME + "\n" +
                            "PORT " + PORT + "\n" +
                            "UUID " + UUID + "\n" +
                            "NAME " + NAME + "\n";

            writeConfig(newConfig);

        }else{
            String[] cl = config.split("\n");
            for(String l : cl){
                String[] cw = l.split(" ");
                switch (cw[0]) {
                    case "HOSTNAME" -> HOSTNAME = cw[1];
                    case "PORT" -> PORT = Integer.parseInt(cw[1]);
                    case "UUID" -> UUID = cw[1];
                    case "NAME" -> NAME = cw[1];
                }
            }
        }

        System.out.println("HOSTNAME: " + HOSTNAME + "\n" +
                "PORT: " + PORT + "\n" +
                "UUID: " + UUID + "\n" +
                "NAME: " + NAME + "\n");

        System.out.print("Updating IP Entry...");
        if(updateIP()) {
            System.out.print("done.");
        }else{
            System.out.println("ERROR");
        }

        System.out.print("\nDownloading Database...");
        ArrayList<Profile> profiles = list();
        System.out.println("done.");

        System.out.println("\n\nNAME\tIP\t\t\tSTATUS");
        for(Profile p : profiles){
            System.out.println(p.name + "\t" + p.IP + "\t" + getHostStatus(p.IP));
        }

    }

    private static String getHostStatus(String ip){
        if(ip.equals(IP)){
            return "LOCAL";
        }

        try {
            InetAddress inet = InetAddress.getByName(ip);
            if (inet.isReachable(10)) {
                return "ONLINE";
            } else {
                return "OFFLINE";
            }
        }catch(Exception e){
            return "OFFLINE";
        }

    }

    private static String getIP(){
        String ip = "";
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            ip = socket.getLocalAddress().getHostAddress();
        }catch(Exception e){
            return "ERROR";
        }

        return ip;
    }

    private static ArrayList<Profile> list(){
        ArrayList<Profile> out = new ArrayList<Profile>();

        String response = query("LIST");
        String[] rl = response.split("#");

        for(String p : rl) {
            String[] e = p.split(" ");
            Profile profile = new Profile(e[0],e[1],e[2]);
            out.add(profile);
        }
        return out;
    }

    private static boolean updateIP(){

        String response = query("UPDATE_IP " + UUID + " " + IP);

        return response.equals("SUCCESS");
    }

    private static String register(String name){
        String response = query("REG_CLIENT " + name + " " + IP);
        String[] rs = response.split(" ");

        if(rs[0].equals("SUCCESS")){
            return rs[1];
        }

        return "ERROR";
    }

    private static String query(String request){

        try {
            Socket echoSocket = new Socket(HOSTNAME, PORT);
            PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
            out.println(request);
            return in.readLine();
        }catch(Exception e){
            e.printStackTrace();
        }

        return "ERROR";
    }


    private static void writeConfig(String config){
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("./client.conf"))) {
            String fileContent = config;
            bufferedWriter.write(fileContent);
        } catch (IOException e) {
           e.printStackTrace();
        }
    }

    private static String readConfig(){
        String config = "";
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader("./client.conf"))) {
            String line = bufferedReader.readLine();
            while(line != null) {
                config += (line) + "\n";
                line = bufferedReader.readLine();
            }
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }



}
