/* Name: ComputerPlayer
 * Author: Devon McGrath
 * Description: This class represents a computer player which can update the
 * game state without user interaction.
 */

package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import logic.MoveGenerator;

/**
 * The {@code ComputerPlayer} class represents a computer player and updates
 * the board based on a model.
 */

public class MinMaxPlayer extends ComputerPlayer {

	protected boolean player;
	protected int calls;
	protected StateSet transpositionTableMax, transpositionTableMin;

	public MinMaxPlayer(boolean joueur) {
		this.player = joueur;
		this.transpositionTableMax = new StateSet();
		this.transpositionTableMin = new StateSet();
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
		Move m = this.MinMax_decision(game, 7, player);
		
		// Actually do the move
		game.move(m.getStartIndex(), m.getEndIndex());
		
	}
	
	// For every move available, calculate the Max value that would result from choosing it
	// Take the best move (the one which leads to highest value)
	public Move MinMax_decision(Game game, int depth, boolean p) {
		
		// Initialise maximum value to negative infinity because we'll try to maximize this value
		double max = Double.NEGATIVE_INFINITY;
		double temp;
		
		// The variable will contain the result i.e a move among the allowed moves
		Move bestMove = null;
		
		// List all possible moves
		List<Move> moves = getMoves(game);
		// This variable tracks the number of calls made at each turn
		// We use it to compare the performance between MinMax and AlphaBeta 
		calls=0;
			
		for (Move m : moves) {
			// Work on a copy to simulate the scenario
			Game copy = game.copy();
			
			// Make the move
			copy.move(m.getStartIndex(), m.getEndIndex());
			
			// Launch recursive analysis
			temp = Min_value(copy, depth-1, !p);
			
			// Update max variable only if an higher value is found
			if(max <= temp) {
				max = temp;
				bestMove =  m;
			}
		} 
		System.out.println("Calls :" + calls);	
	
	
		// Return the best possible move I can do
		return bestMove;
	}

	public double Max_value (Game game, int depth, Boolean turn ) {
		// Initialise maximum value to negative infinity because we'll try to maximise this value
		double max = Double.NEGATIVE_INFINITY;
		// Update the number of calls made
		calls++;
		// If we reached the maximum depth allowed then return the heuristic on the remaining nodes
		if (depth == 0) {
			return game.goodHeuristic(turn);
		}
		// Otherwise take the best move and simulate it, then call Min to simulate his next move
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
					max = Math.max(max, Min_value(copy, depth-1, !turn));
					
					// Save the value for this node
					this.transpositionTableMax.add(copy, (int)max);
				} else {
					// Otherwise take it from transposition matrix
					max = Math.max(max, this.transpositionTableMax.getValue(copy));
				}

			}
		}
		
		// Return the maximum found for this path
		return max;
	}
	
	public double Min_value (Game game, int depth, Boolean turn ) {
		// Initialise minimum value to positive infinity because we'll try to minimise this value
		double min = Double.POSITIVE_INFINITY;
		// Update the number of calls made
		calls++;
		
		// If we reached the maximum depth allowed then return the heuristic on the remaining nodes
		if (depth == 0) {
			return game.goodHeuristic(turn);
		}
		// Otherwise take the best move and simulate it, then call Max to simulate his next move
		else {
			// Get all possible moves
			List<Move> moves = getMoves(game);	
			for(Move m : moves) {
				// Work on a copy to avoid loosing the original composition of the game
				Game copy = game.copy();
				
				// Make the move
				copy.move(m.getStartIndex(), m.getEndIndex());
				
				// If we haven't already calculated the value for this node then keep going
				if(this.transpositionTableMin.getValue(copy) == null) {
					min = Math.min(min, Max_value(copy, depth-1, !turn));
					
					// Save the value for this node
					this.transpositionTableMin.add(copy, (int)min);
				} else {
					// Otherwise take it from transposition matrix
					min = Math.min(min, this.transpositionTableMin.getValue(copy));
				}
			}
		}

		// Return the minimum found for this path
		return min;
	}

	
	/**
	 * Gets all the available moves and skips for the current player.
	 * 
	 * @param game	the current game state.
	 * @return a list of valid moves that the player can make.
	 */
	protected List<Move> getMoves(Game game) {
		
		// The next move needs to be a skip
		if (game.getSkipIndex() >= 0) {
			
			List<Move> moves = new ArrayList<>();
			List<Point> skips = MoveGenerator.getSkips(game.getBoard(),
					game.getSkipIndex());
			for (Point end : skips) {
				Game copy = game.copy();
				int startIndex = game.getSkipIndex(), endIndex = Board.toIndex(end);
				copy.move(startIndex,endIndex);
				moves.add(new Move(startIndex, endIndex, copy.goodHeuristic(!copy.isP2Turn())));
			}
			Collections.sort(moves);
			return moves;
		}
		
		// Get the checkers
		List<Point> checkers = new ArrayList<>();
		Board b = game.getBoard();
		if (game.isP2Turn()) {
			checkers.addAll(b.find(Board.BLACK_CHECKER));
			checkers.addAll(b.find(Board.BLACK_KING));
		} else {
			checkers.addAll(b.find(Board.WHITE_CHECKER));
			checkers.addAll(b.find(Board.WHITE_KING));
		}
		
		// Determine if there are any skips
		List<Move> moves = new ArrayList<>();
		for (Point checker : checkers) {
			int index = Board.toIndex(checker);
			List<Point> skips = MoveGenerator.getSkips(b, index);
			for (Point end : skips) {
				Game copy = game.copy();
				int endIndex = Board.toIndex(end);
				copy.move(index,endIndex);
				Move m = new Move(index, endIndex, copy.goodHeuristic(!copy.isP2Turn()));
				moves.add(m);
			}
		}
		
		// If there are no skips, add the regular moves
		if (moves.isEmpty()) {
			for (Point checker : checkers) {
				int index = Board.toIndex(checker);
				List<Point> movesEnds = MoveGenerator.getMoves(b, index);
				for (Point end : movesEnds) {
					Game copy = game.copy();
					int endIndex = Board.toIndex(end);
					copy.move(index,endIndex);
					moves.add(new Move(index, endIndex, copy.goodHeuristic(!copy.isP2Turn())));
				}
			}
		}
		Collections.sort(moves);
		return moves;
	}
}
