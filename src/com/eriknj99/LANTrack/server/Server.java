package com.eriknj99.LANTrack.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    ArrayList<Profile> profiles;

    private int UUID_LEN = 10;
    private int PORT = 3333;

    public Server(){
        profiles = loadConfig();

        printListings();

        listen();

    }

    public void printListings(){
        System.out.println("----LISTINGS----");
        System.out.println("NAME\tIP\t\t\tUUID");
        for(Profile p : profiles){
            System.out.println(p.name + "\t" + p.IP + "\t" + p.UUID);
        }
    }

    private void listen(){
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            while(true) {
                Socket clientSocket = serverSocket.accept();
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out.println(process_request(in.readLine()));
                clientSocket.close();
            }

        }catch(Exception e){ e.printStackTrace(); }
    }

    private String process_request(String request){

        System.out.println("RECEIVE >> " + request);

        String[] rq = request.split(" ");

        if(rq[0].equals("REG_CLIENT")){

            System.out.print("\tCLIENT_REG -> DUPLICATE_NAME_CHECK -> ");
            for(Profile p : profiles){
                if(p.name.equals(rq[1])){
                    System.out.println("FAIL");
                    return "FAIL";
                }
            }
            System.out.println("PASS");

            Profile p = new Profile(rq[1],genUUID(),rq[2]);
            profiles.add(p);

            System.out.println("\tCLIENT_REG -> SUCCESS");
            System.out.println("\t\tNAME -> " + p.name);
            System.out.println("\t\tUUID -> " + p.UUID);
            System.out.println("\t\tIP   -> " + p.IP);

            saveConfig();
            return "SUCCESS " + p.UUID;

        }else if(rq[0].equals("UPDATE_IP")){
            String uuid = rq[1];
            String ip = rq[2];
            for(Profile p : profiles){
                if(p.UUID.equals(uuid)){
                    System.out.println("UPDATE_IP -> SUCCESS");
                    System.out.println("\t\tNAME -> " + p.name);
                    System.out.println("\t\tUUID -> " + p.UUID);
                    System.out.println("\t\tIP   -> " + p.IP + "->" + ip);

                    p.IP = ip;
                    saveConfig();
                    return "SUCCESS";
                }
            }
            System.out.println("UPDATE_IP -> FAIL");
            return "FAIL";
        }else if(rq[0].equals("LIST")){
            System.out.println("SEND_LISTINGS -> SUCCESS");
            String out = "";
            for(Profile p : profiles){
                out += p.name + " " + p.IP + "#";
            }
            System.out.println(out);
            return out;
        }else if(rq[0].equals("UNREG_CLIENT")){
            String uuid = rq[1];
            System.out.print("UNREGISTER -> ");
            for(int i = profiles.size() - 1; i >= 0; i--){
                if(profiles.get(i).UUID.equals(uuid)){
                    profiles.remove(i);
                    System.out.println("SUCCESS");
                    printListings();
                    saveConfig();
                    return "SUCCESS";
                }
            }
            System.out.println("FAIL");

            return "FAIL";
        }

        return "INVALID_REQUEST";
    }

    private String genUUID(){
        String UUID  = "";
        for(int i = 0; i < UUID_LEN; i++){
            UUID += ""+(int)(Math.random() * 9);
        }
        //In the unlikely case of a collision try again.
        for(Profile p : profiles){
            if(p.UUID.equals(UUID)){
                return genUUID();
            }
        }
        return UUID;
    }

    // Writes the contents of string 'config' to the file 'server.conf'
    private static void writeConfig(String config){
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("./server.conf"))) {
            String fileContent = config;
            bufferedWriter.write(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Reads the contents of the file 'server.conf'
    private static String readConfig(){
        String config = "";
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader("./server.conf"))) {
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

    // Save the current profiles to the config file
    private  void saveConfig(){
        String newConfig = "";
        for(Profile p : profiles){
            newConfig += p.name + " " + p.UUID + " " + p.IP+"\n";
        }
        writeConfig(newConfig);
    }

    // Load the current profiles from the config file
    private ArrayList<Profile> loadConfig(){
        ArrayList<Profile> out = new ArrayList<Profile>();

        String config = readConfig();
        if(config == null){
            writeConfig("");
            return out;
        }
        String[] cl = config.split("\n");

        for(String p : cl) {
            String[] e = p.split(" ");
            Profile profile = new Profile(e[0],e[1],e[2]);
            out.add(profile);
        }
        return out;
    }

}
