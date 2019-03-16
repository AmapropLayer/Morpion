package com.github.AmapropLayer.morpion;

import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.server.Server;

public class Main {
	
    public static void main(String[] args) {
    	
    	// Connection à Discord
    	String token = "NTQwODg4NDkyNDA2NzM0ODc5.DzXggQ.OizEFg-euzs4fjxXR7zla6a7vXM";
    	
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