package com.github.AmapropLayer.morpion;

import java.util.TimerTask;

import org.javacord.api.entity.channel.TextChannel;

public class TimerGame extends TimerTask {

	TextChannel channel;
	ServerHandler sh;
	
	public TimerGame(TextChannel channel, ServerHandler sh) {
		this.channel = channel;
		this.sh = sh;
	}
	
	@Override
	public void run() {
		channel.sendMessage(sh.getNextPlayer().getMentionTag() + sh.getTimeoutSentence());
		sh.finishGame();
	}

}
