package net.demilich.metastone.game.behaviour.mcts;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.TurnState;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.logic.GameLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class Node {

	private GameContext state;
	private final GameLogic logic = new GameLogic();
	private List<GameAction> validTransitions;
	private final List<Node> children = new LinkedList<>();
	private final GameAction incomingAction;
	private int visits;
	private int score;
	private final int player;

	private static final Logger logger = LoggerFactory.getLogger(Node.class);

	public Node(GameAction incomingAction, int player) {
		this.incomingAction = incomingAction;
		this.player = player;
	}

	private boolean canFurtherExpanded() {
		return !validTransitions.isEmpty();
	}

	private Node expand() {
		GameAction action = validTransitions.remove(0);
		GameContext newState = state.clone();

		for (Player player : newState.getPlayers()) {
			player.setBehaviour(new PlayRandomBehaviour());
		}

		try {
			newState.getLogic().performGameAction(newState.getActivePlayer().getId(), action);
		} catch (Exception e) {
			System.err.println("Exception on action: " + action + " state decided: " + state.gameDecided());
			e.printStackTrace();
			throw e;
		}

		Node child = new Node(action, getPlayer());

		/**-------------------------------------------------*/

		if (newState.getTurnState() == TurnState.TURN_ENDED){
			newState.startTurn(newState.getActivePlayer().getId());
		}
		/**--------------------------------------------------*/
		child.initState(newState, newState.getValidActions());

		children.add(child);
		return child;
	}

	public GameAction getBestAction() {
		GameAction best = null;
		//int bestScore = Integer.MIN_VALUE;
		int mostVisits = Integer.MIN_VALUE;
		for (Node node : children) {
			//if (node.getScore() > bestScore) {
			if (node.getVisits() > mostVisits) {
				best = node.incomingAction;
				//bestScore = node.getScore();
				mostVisits = node.getVisits();
			}
		}
		return best;
	}

	public List<Node> getChildren() {
		return children;
	}

	public GameAction getIncomingAction() { return incomingAction; }

	public int getPlayer() {
		return player;
	}

	public int getScore() {
		return score;
	}

	public GameContext getState() {
		return state;
	}

	public int getVisits() {
		return visits;
	}

	public void initState(GameContext state, List<GameAction> validActions) {
		this.state = state.clone();
		this.validTransitions = new ArrayList<GameAction>(validActions);
	}

	/**-------------------------------------------------*/
	public void rootInitState(GameContext state, List<GameAction> validActions) {
		this.state = state.clone();
		this.validTransitions = new ArrayList<GameAction>(validActions);

		//Replace opponent's hand with a random one
		this.logic.setContext(this.state);
		for (Player pl : this.state.getPlayers()){
			if (pl.getId()!= this.state.getActivePlayerId()) {
				boolean[] randomFunction = {false, true};
				this.logic.getRandomHand(pl, randomFunction);
			}
		}
	}
	/**-------------------------------------------------*/

	public boolean isExpandable() {
		if (validTransitions.isEmpty()) {
			return false;
		}
		if (state.gameDecided()) {
			return false;
		}
		return getChildren().size() < validTransitions.size();
	}

	public boolean isLeaf() {
		return children == null || children.isEmpty();
	}

	private boolean isTerminal() {
		return state.gameDecided();
	}

	public void process(ITreePolicy treePolicy) {
		List<Node> visited = new LinkedList<Node>();
		Node current = this;
		visited.add(this);
		while (!current.isTerminal()) {
			if (current.canFurtherExpanded()) {
				current = current.expand();
				visited.add(current);
				break;
			} else {
				current = treePolicy.select(current);
				visited.add(current);
			}
		}
		int value = rollOut(current);
		for (Node node : visited) {
			//logger.info("Action: {}, playerId: {}", node.incomingAction, node.getPlayer());
			node.updateStats(value);
		}
	}

	public int rollOut(Node node) {
		if (node.getState().gameDecided()) {
			GameContext state = node.getState();
			return state.getWinningPlayerId() == getPlayer() ? 1 : 0;
		}

		GameContext simulation = node.getState().clone();
		for (Player player : simulation.getPlayers()) {
			player.setBehaviour(new PlayRandomBehaviour());
		}
		simulation.playFromStateAction(player);

		return simulation.getWinningPlayerId() == getPlayer() ? 1 : 0;
	}

	private void updateStats(int value) {
		visits++;
		score += value;
	}

}
