package com.github.AmapropLayer.morpion;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

public class ServerHandler implements Runnable {
	
	// List de chaines utilisées
	private final String value_first = ":o:";
	private final String value_second = ":x:";
	private final String value_empty = ":black_large_square:";
	private final String command_start = "!morpion";
	private final String command_play = "!mplay";
	private final String command_surrender = "!surrender";
	private final String command_help = "!mhelp";
	private final String game_surrender = "Drapeau blanc. Abandon de ";
	private final String game_winner = "Gagnant : ";
	private final String game_tie = "Egalité.";
	private final String game_timeout = "Vous avez été trop lent... Vous avez donc perdu.";
	private final String error_toomanyplayers = "Uniquement deux personnes peuvent s'affronter.";
	private final String error_nogame = "Aucune partie n'est lancée... Utilisez "+ command_start + " pour en commencer une.";
	private final String error_notaplayer = " c'est pas ta guerre.";
	private final String error_notaposition = "Entrée invalide.";
	private final String error_notyourturn = "Ce n'est pas à vous de jouer.";
	private final String error_bot = "Désolé, les bots ne peuvent pas jouer.";
	private final String error_opengame = "Une partie est déjà en cours,";
	private final String defie = " vous avez été défié au morpion par ";
	private final String help = "```" + command_start + " @user - lance une partie\n" + command_play + " int int - joue sur les positions données (0->2 pour la ligne, 0->2 pour la colonne)\n"
			+ command_surrender + " - abandon de la partie\n" + command_help + " - affiche ce message\n\nLe joueur qui lance la partie commence."
					+ "\nSi vous n'avez pas joué dans les 3 minutes qui suivent, vous êtes déclaré perdant.```";
	
	// Attributs nécessaires
	private Grid grid = null;
	private User player1 = null;
	private User player2 = null;
	private User nextPlayer = null;
	private Server server;
	private int nextSymbol;
	private DiscordApi api;
	private Timer timer;
	private TimerTask timertask;
	
	/**
	 * Contructeur
	 * @param api L'API
	 * @param server Serveur que le bot a rejoint
	 */
	public ServerHandler(DiscordApi api, Server server) {
		this.api = api;
		this.server = server;
		timer = new Timer();
		System.out.println("Connecté sur le serveur " + server.getId());
	}
	
	public void run() {
		// Création d'un listener
        server.addMessageCreateListener(event -> {
        	if(event.getMessageAuthor() != api.getYourself()) {
        		if(event.getMessageContent().startsWith(command_start)) {
            		startGame(event);
            	} else if(event.getMessageContent().startsWith(command_play)) {
            		playGame(event);
            	} else if(event.getMessageContent().startsWith(command_surrender)) {
            		surrenderGame(event);
            	} else if(event.getMessageContent().startsWith(command_help) || event.getMessage().getMentionedUsers().contains(api.getYourself())) {
            		displayHelp(event);
            	}
        	}
        });
	}
	
	private void displayHelp(MessageCreateEvent event) {
		event.getChannel().sendMessage(help);
	}
	
    /**
     * Lancement d'une partie
     * @param event Evenement ayant déclanché cet appel
     */
    private void startGame(MessageCreateEvent event) {
    	List<User> listUser = event.getMessage().getMentionedUsers();
    	User author = event.getMessageAuthor().asUser().get();
    	TextChannel channel = event.getChannel();
    	if(verifyStartGameMessage(listUser, author, channel)) {
    		// Lancement d'un morpion
    		player1 = author;
    		nextPlayer = player1;
    		player2 = listUser.get(0);
    		nextSymbol = 1;
    		event.getChannel().sendMessage(player2.getMentionTag() + defie + player1.getMentionTag());
    		grid = new Grid();
    		event.getChannel().sendMessage(getDisplayableGrid());
    		startTimer(event.getChannel());
    	}
    }
    
    /**
     * Lance un timer de 3 minutes, efface la partie en cours si le joueur ne joue pas
     * @param channel Channel sur lequel écrire le message
     */
    private void startTimer(TextChannel channel) {
    	timertask = new TimerGame(channel, this);
    	timer.schedule(timertask, 3*60*1000);
    }
    
    /**
     * Regarde si les astres sont alignés pour le lancement d'une partie
     * @param listUser Liste des utilisateurs taggés dans la requête de partie
     * @param author Auteur de la requête
     * @param channel Channel de discussion
     * @return true si la partie est possible, false sinon
     */
    private boolean verifyStartGameMessage(List<User> listUser, User author, TextChannel channel) {
    	if(grid != null) {
    		sendError(channel, error_opengame);
    		return false;
    	}
    	if(listUser.isEmpty()) {
    		sendError(channel,"Il n'est pas encore possible de jouer face à moi... Merci de choisir un adversaire.");
			return false;
		}
    	if (author.isBot() || listUser.get(0).isBot()){
    		sendError(channel, error_bot);
			return false;
		}
    	if (listUser.size() > 1){
    		sendError(channel, error_toomanyplayers);
			return false;
		}
    	return true;
    }
    
    /**
     * Participation a la partie
     * @param event Evenement ayant déclanché cet appel
     */
    private void playGame(MessageCreateEvent event) {
    	if(grid == null) {
    		// Aucune partie n'est lancée
			sendError(event.getChannel(), error_nogame);
		} else {
    		// Participation au morpion
    		String[] data = event.getMessageContent().split(" ");
    		try {
	    		if(event.getMessageAuthor().getId() == nextPlayer.getId()) {
	    			timertask.cancel();
	    			// On vérifie le nombre d'arguments
	    			if(data.length < 3) {
	    				sendError(event.getChannel(), error_notaposition);
	    			}else {
	    				Integer posx = Integer.parseInt(data[1]);
	    				Integer posy = Integer.parseInt(data[2]);
	    				// On vérifie les valeurs des entrées
	    				if(posx < 0 || posx > 2 || posy < 0 || posy > 2) {
	    					sendError(event.getChannel(), error_notaposition);
	    				} else {
	    					int result = grid.play(nextSymbol, posx, posy);
			        		displayResult(result, event);
			        		// On affiche la grille
			        		event.getChannel().sendMessage(getDisplayableGrid());
			        		if(result > 0) {
			        			finishGame();
			        		} else if(result == 0){
			        			startTimer(event.getChannel());
			        			if(nextSymbol == 1) {
			        				nextSymbol = 2;
			        			} else {
			        				nextSymbol = 1;
			        			}
			        			if(nextPlayer.getId() == player1.getId()) {
			        				nextPlayer = player2;
			        			} else {
			        				nextPlayer = player1;
			        			}
			        		}
	    				}
	    			}
	    		} else {
	    			if(event.getMessageAuthor().getId() != nextPlayer.getId()) {
	    				sendError(event.getChannel(), error_notyourturn);
	    			}
	    		}
    		}catch (NumberFormatException nfe) {
    			startTimer(event.getChannel());
    			displayResult(-1, event);
    			event.getChannel().sendMessage(getDisplayableGrid());
    		}
		}
    }
    
    /**
     * Affichage de la phrase correspondant au paramètre de résultat
     * @param result -1 = erreur, 0 = rien, 1 = victoire j1, 2 = victoire j2, 3 = egalité
     * @param event Evenement ayant déclenché cet appel
     */
    private void displayResult(int result, MessageCreateEvent event) {
    	if(result == -1) {
    		event.getChannel().sendMessage(error_notaposition);
    	} else if(result == 1) {
    		event.getChannel().sendMessage(game_winner + player1.getMentionTag());
    	} else if(result == 2) {
    		event.getChannel().sendMessage(game_winner + player2.getMentionTag());
    	} else if(result == 3) {
    		event.getChannel().sendMessage(game_tie);
    	}
    }
    
    /**
     * Abandon de la partie
     * @param event Evenement ayant déclanché l'abandon
     */
    private void surrenderGame(MessageCreateEvent event) {
    	if(grid == null) {
    		event.getChannel().sendMessage(error_nogame);
    	} else {
    		// Fermeture de la partie en cours
    		User dude = event.getMessageAuthor().asUser().get();
    		if(dude.getId() == player1.getId() || dude.getId() == player2.getId()) {
        		event.getChannel().sendMessage(game_surrender + dude.getNicknameMentionTag());
        		finishGame();
        		timertask.cancel();
    		} else {
    			event.getChannel().sendMessage(dude.getNicknameMentionTag()+ error_notaplayer);
    		}
    	}
    }
    
    /**
     * Les variables nécessaires à une partie sont mises à null
     */
    protected void finishGame() {
    	grid = null;
    	player1 = null;
    	player2 = null;
    	nextPlayer = null;
    }
    
    /**
     * Retourne la grille kawaiiiiiiii ahahahaahah tamer
     * @return La grille formatée pour Discord
     */
    private String getDisplayableGrid() {
    	return grid.toString().replaceAll("1", value_first).replaceAll("2", value_second).replaceAll("0", value_empty);
    }
    
    public User getNextPlayer() {
    	return nextPlayer;
    }
    
    public String getTimeoutSentence() {
    	return game_timeout;
    }
    
    private void sendError(TextChannel c, String error) {
    	c.sendMessage(error);
    }
}
