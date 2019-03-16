package com.github.AmapropLayer.morpion;

public class Grid {
	private int[] grid;
	private final Integer defaultValue = 0;
	private final Integer firstPlayerValue = 1;
	private final Integer secondPlayerValue = 2;
	
	public Grid() {
		grid = new int[9];
		for(int i=0; i<9; i++) {
			grid[i] = defaultValue;
		}
	}
	
	/**
	 * 
	 * @param numPlayer joueur 1 ou 2
	 * @param xcoords coordonnées en x
	 * @param ycoords coordonnées en y
	 * @return -1 = erreur / 0 = pas de gagnant / 1 = joueur 1 gagne / 2 = joueur 2 gagne / 3 = egalité
	 */
	public int play(int numPlayer, int xcoords, int ycoords) {
		int position;
		if(numPlayer != 1 && numPlayer != 2) {
			return -1;
		}
		position = (xcoords * 3) + (ycoords);
		if(position > 8 || position < 0) {
			return -1;
		} else if(grid[position] != 0) {
			return -1;
		}
		if(numPlayer == 1) {
			grid[position] = firstPlayerValue;
			return verifyGrid();
		} else {
			grid[position] = secondPlayerValue;
			return verifyGrid();
		}
	}
	
	/**
	 * Fonction de vérification de la grille
	 * @return 0 = pas de gagnant / 1 = joueur 1 a gagné / 2 = joueur 2 a gagné / 3 = egalité
	 */
	public int verifyGrid() {
		int winner = 0;
		if(grid[0] == grid[4] && grid[0] == grid[8]) {
			// Diagonale depuis 0,0
			winner = grid[0];
		}
		if(grid[2] == grid[4] && grid[2] == grid[6]) {
			// Diagonale depuis 0,2
			winner = grid[2];
		} 
		for(int i=0; i<3; i++) {
			if(grid[i*3] == grid[(i*3)+1] && grid[(i*3)] == grid[(i*3)+2]) {
				// Chaque ligne
				winner = grid[i*3];
			} else if(grid[i] == grid[i+3] && grid[i] == grid[i+6]) {
				// Chaque colonne
				winner = grid[i];
			}
		}
		
		// On regarde si il n'y a pas égalité
		if(winner == 0) {
			int i=0;
			boolean trouve = false;
			do {
				if(grid[i] == 0) {
					trouve = true;
				}
				i++;
			} while(i < 9 && !trouve);
			if(trouve == false) {
				winner = 3;
			}
		}
		
		return winner;
	}


	public int[] getGrid() {
		return grid;
	}
	
	public String toString() {
		String chaine = "";
		for(int i = 0; i<9; i=i+3) {
			chaine+=grid[i]+" "+grid[i+1]+" "+grid[i+2]+"\n";
		}
		return chaine;
	}
}
