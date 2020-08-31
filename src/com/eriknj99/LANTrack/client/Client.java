package com.eriknj99.LANTrack.client;

import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {

    private String HOSTNAME;
    private int PORT;
    private String UUID;
    private String NAME;
    private String IP;


    public Client(){
        IP = getIP();
        loadConfig();
    }

    public void runConfigWizard(){
        System.out.println("---Config Wizard---");
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter Server hostname: ");
        HOSTNAME = sc.nextLine();

        boolean validPort = false;

        while(!validPort) {
            System.out.print("\nEnter Server port: ");
            try {
                PORT = Integer.parseInt(sc.nextLine());
                validPort = true;
            }catch(Exception e){
                //Do Nothing
            }
        }

        System.out.print("\nEnter a name for this machine:");
        NAME = sc.nextLine();

        String regResponse = register(NAME);
        if(!regResponse.equals("ERROR")){
            UUID = regResponse;
        }else{

            // If an error occurs while registering client prompt user to retry
            System.out.println("An error occurred while testing testing the following configuration:");
            System.err.print("HOSTNAME " + HOSTNAME + "\n" +
                               "PORT " + PORT + "\n" +
                               "NAME " + NAME + "\n");

            System.out.println("Would you like to retry? (Y/N)");
            String r = "";
            do {
                System.out.print("(Y/N)");
                r = sc.nextLine().toUpperCase();

            }while(!r.equals("Y") && !r.equals("N"));

            if(r.equals("Y")){
                runConfigWizard(); //If the user chose 'Y'; Recursively restart the wizard.
                return;
            }
            //If the user chose 'N'; revert to the previous config and return.
            loadConfig();
            return;
        }

        String newConfig = "HOSTNAME " + HOSTNAME + "\n" +
                "PORT " + PORT + "\n" +
                "UUID " + UUID + "\n" +
                "NAME " + NAME + "\n";

        writeConfig(newConfig);
    }

    private String getHostStatus(String ip){
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

    private String getIP(){
        String ip = "";
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            ip = socket.getLocalAddress().getHostAddress();
        }catch(Exception e){
            return "ERROR";
        }

        return ip;
    }

    public ArrayList<Profile> list(){
        ArrayList<Profile> out = new ArrayList<Profile>();

        String response = query("LIST");
        String[] rl = response.split("#");

        for(String p : rl) {
            String[] e = p.split(" ");
            Profile profile = new Profile(e[0],e[1]);
            out.add(profile);
        }
        return out;
    }

    public boolean updateIP(){
        IP = getIP();
        String response = query("UPDATE_IP " + UUID + " " + IP);
        return response.equals("SUCCESS");
    }

    public String register(String name){
        String response = query("REG_CLIENT " + name + " " + IP);
        String[] rs = response.split(" ");

        if(rs[0].equals("SUCCESS")){
            return rs[1];
        }

        return "ERROR";
    }

    public boolean unregister(){
        String response = query("UNREG_CLIENT " + UUID);
        if(response.equals("SUCCESS")){
            UUID = "-";
            NAME = "-";
            String newConfig = "HOSTNAME " + HOSTNAME + "\n" +
                    "PORT " + PORT + "\n" +
                    "UUID " + UUID + "\n" +
                    "NAME " + NAME + "\n";

            writeConfig(newConfig);
            return true;
        }
        return false;
    }

    private String query(String request){

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


    private void writeConfig(String config){
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("./client.conf"))) {
            bufferedWriter.write(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readConfig(){
        String config = "";
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader("./client.conf"))) {
            String line = bufferedReader.readLine();
            while(line != null) {
                config += (line) + "\n";
                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            return null;
        }
        return config;
    }

    private void loadConfig(){
        String config = readConfig();

        if(config == null) {
            runConfigWizard();
        }else {
            String[] cl = config.split("\n");
            for (String l : cl) {
                String[] cw = l.split(" ");
                switch (cw[0]) {
                    case "HOSTNAME" -> HOSTNAME = cw[1];
                    case "PORT" -> PORT = Integer.parseInt(cw[1]);
                    case "UUID" -> UUID = cw[1];
                    case "NAME" -> NAME = cw[1];
                }
            }
        }
    }

    public String getListing(){
        String out = "";
        ArrayList<Profile> profiles = list();

        out+=("NAME\tIP\t\t\tSTATUS\n");
        for(Profile p : profiles){
           out+=(p.name + "\t" + p.IP + "\t" + getHostStatus(p.IP)+"\n");
        }

        return out;
    }

    public String toString(){
        return("HOSTNAME: " + HOSTNAME + "\n" +
                "PORT: " + PORT + "\n" +
                "UUID: " + UUID + "\n" +
                "NAME: " + NAME + "\n");
    }
}
