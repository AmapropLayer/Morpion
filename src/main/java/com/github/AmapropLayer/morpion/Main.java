package com.github.AmapropLayer.morpion;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.server.Server;

public class Main {
	
	private final static String filename = "token.txt";
	
    public static void main(String[] args) {
    	
    	// Récupération du Token Discord
    	String token = null;
    	try {
    		FileReader fr = new FileReader(filename);
    		BufferedReader br = new BufferedReader(fr);
    		token = br.readLine();
    		br.close();
    		fr.close();
    	} catch (FileNotFoundException ex) {
    		System.err.println("Unable to open file '" + filename + "'.");
    	} catch (IOException ex) {
    		System.err.println("Error reading file '" + filename + "'.");
    	}
    	
    	// Connexion à Discord
    	new DiscordApiBuilder().setToken(token).login().thenAccept(api -> {
    		for(Server s : api.getServers()) {
    			Thread t = new Thread(new ServerHandler(api, s));
    			t.start();
    		}
    		api.addServerJoinListener(event -> {
    			Thread t = new Thread(new ServerHandler(api, event.getServer()));
    			t.start();
    		});
            System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());
    	}); 
    }
}
