package com.github.AmapropLayer.morpion;

import org.javacord.api.entity.server.Server;

public class Pair {
	private Server server;
	private Grid grid;
	
	public Pair(Server server) {
		this.server = server;
	}
	
	public void setGrid(Grid grid) {
		this.grid = grid;
	}
	
	public Server getServer() {
		return server;
	}
	
	public Grid getGrid() {
		return grid;
	}
}
