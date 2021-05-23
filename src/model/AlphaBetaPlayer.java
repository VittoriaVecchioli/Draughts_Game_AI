/* Name: ComputerPlayer
 * Author: Devon McGrath
 * Description: This class represents a computer player which can update the
 * game state without user interaction.
 */

package model;

import java.util.List;

/**
 * The {@code ComputerPlayer} class represents a computer player and updates
 * the board based on a model.
 */
public class AlphaBetaPlayer extends MinMaxPlayer {

	//public boolean player;
	
	public AlphaBetaPlayer(boolean joueur) {
		super(joueur);
	}
	@Override
	public boolean isHuman() {
		return false;
	}

	@Override
	public void updateGame(Game game) {
		
		// Nothing to do
		if (game == null || game.isGameOver()) {
			return;
		}
		// reset the transposition tables
		this.transpositionTableMax = new StateSet();
		this.transpositionTableMin = new StateSet();
		
		// Check if it is my turn to play
		if(game.isP2Turn()!= player) {
			return;
		}
		// Get the best move that can be done
		Move m = this.alpha_beta_search(game, 7, player);
		
		// Actually do the move
		game.move(m.getStartIndex(), m.getEndIndex());
		
	}
	
	public Move alpha_beta_search(Game game, int depth, boolean player) {
		// Initialise alpha to negative infinity as we'll try to maximise Max's score
		double alpha = Double.NEGATIVE_INFINITY;
		// Initialise beta to positive infinity as we'll try to minimise Min's score
		double beta = Double.POSITIVE_INFINITY;
		
		// Initialise maximum value to negative infinity because we'll try to maximise this value
		double max = Double.NEGATIVE_INFINITY;
		
		double temp;
		// This variable will contain the resulting move i.e a move among the allowed moves
		Move bestMove = null;
		
		// This variable tracks the number of calls made at each turn
		// We use it to compare the performance between MinMax and AlphaBeta 
		calls=0;
		
		// List all possible moves
		List<Move> moves = getMoves(game);
		for (Move m : moves) {
			// Work on a copy to simulate the scenario
			Game copy = game.copy();
			// Make the move
			copy.move(m.getStartIndex(), m.getEndIndex());
			
			// Launch recursive analysis
			temp = min_value(copy, depth-1, !player, alpha, beta);
			
			// Update max variable only if an higher value is found
			if(max <= temp) {
				max = temp;
				bestMove = m;
			}	
		} 
		
		System.out.println("Calls :" + calls);

		return bestMove;
	}
	
	public double max_value(Game game, int depth, boolean turn, double alpha, double  beta){
		// Initialise maximum value to negative infinity because we'll try to maximise this value
		double max = Double.NEGATIVE_INFINITY;
		// Update the number of calls made
		calls++;
		// If we reached the maximum depth allowed then return the heuristic on the remaining nodes
		if (depth ==0) {
			return game.goodHeuristic(turn);
		}
		else {
			// Get all possible moves
			List<Move> moves= getMoves(game);

			for(Move m:moves) {
				
				// Work on a copy to avoid loosing the original composition of the game
				Game copy = game.copy();
				// Make the move
				copy.move(m.getStartIndex(), m.getEndIndex());
				
				// If we haven't already calculated the value for this node then keep going
				if(this.transpositionTableMax.getValue(copy) == null) {
					max = Math.max(max, min_value(copy, depth-1, !turn, alpha, beta));
					
					// Save the value for this node
					this.transpositionTableMax.add(copy, (int)max);
				} else {
					// Otherwise take it from transposition matrix
					max = Math.max(max, this.transpositionTableMax.getValue(copy));
				}
				// If the value of max is higher than beta, we don't have to visit the rest of the tree 
				// max is the best possible value for the MAX player 
				// So we prune and return max
				
				if(max >= beta) {
					
					return max;
				}
				// We update the value of alpha to the highest value we found along the path for MAX
				alpha = Math.max(alpha, max);
				}
			}
			
		return max;
	}
	
	public double min_value(Game game, int depth, boolean turn, double alpha, double  beta){
		// Initialise minimum value to positive infinity because we'll try to minimise this value
		double min = Double.POSITIVE_INFINITY;
		// Update the number of calls made
		calls++;
		// If we reached the maximum depth allowed then return the heuristic on the remaining nodes
		if (depth ==0) {
			return game.goodHeuristic(turn);
		}
		else {
			// Get all possible moves
			List<Move> moves = getMoves(game);

			for(Move m : moves) {
				// Make the move
				Game copy = game.copy();
				copy.move(m.getStartIndex(), m.getEndIndex());
				
				// If we haven't already calculated the value for this node then keep going
				if(this.transpositionTableMin.getValue(copy) == null) {
					min = Math.min(min, max_value(copy, depth-1, !turn, alpha, beta));
					
					// Save the value for this node
					this.transpositionTableMin.add(copy, (int)min);
				} else {
					// Otherwise take it from transposition matrix
					min = Math.min(min, this.transpositionTableMin.getValue(copy));
				}
				
				// If the value of min is lower than aloha, we don't have to visit the rest of the tree 
				// min is the best possible value for the MIN player 
				// So we prune and return min
				if(min <= alpha) {
					return min;
				}
				// We update the value of beta to the lowest value we found along the path for MIN
				beta = Math.min(beta, min);
			}

		}
		return min;
		
	}
}
