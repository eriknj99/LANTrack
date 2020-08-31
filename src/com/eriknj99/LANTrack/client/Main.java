package com.eriknj99.LANTrack.client;

import java.util.Scanner;

public class Main {

    public static void main(String[] args){
        System.out.println("--- LANTRACK CLIENT V0.1 ---");
        Client c = new Client();
        System.out.println(c.toString());
        c.updateIP();
        System.out.println(c.getListing());
        if(args.length > 0){
            for(String command : args){
                System.out.println("\n>"+command.toUpperCase()+"\n");
                processCommand(command.toUpperCase(), c);
            }
            if(args[0].equals("SHELL")){
                Scanner sc = new Scanner(System.in);
                while(true){
                    System.out.print("\n>");
                    String command = sc.nextLine();
                    processCommand(command.toUpperCase(),c);
                }
            }

        }
    }

    private static void processCommand(String command, Client c){
        switch(command){
            case "UPDATE" -> c.updateIP();
            case "LIST" -> System.out.println(c.getListing());
            case "SHOWCFG" -> System.out.println(c.toString());
            case "EDITCFG" -> c.runConfigWizard();
            case "UNREG" -> System.out.print(c.unregister()?"SUCCESS":"FAIL");
            case "HELP" -> System.out.println("UPDATE : Update IP entry\nLIST : Print Current IP Listings\nSHOWCFG : Print current config\nEDITCFG : Launch Config Wizard\nSHELL : Start an interactive shell (arg only)");
            case "SHELL" -> System.out.print(""); // Do nothing
            default -> System.out.println("Invalid Command : Type \'HELP\' for a complete list of commands.");
        }
    }

}
