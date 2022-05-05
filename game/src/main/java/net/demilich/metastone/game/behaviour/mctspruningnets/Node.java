package net.demilich.metastone.game.behaviour.mctspruningnets;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.TurnState;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.behaviour.models.JsonReader;
import net.demilich.metastone.game.collect.data.CsvHandler;
import net.demilich.metastone.game.logic.GameLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


class Node {

	private GameContext state;
	private final GameLogic logic = new GameLogic();
	private List<GameAction> validTransitions;
	private final List<Node> children = new LinkedList<>();
	private final GameAction incomingAction;
	private int visits;
	private int score;
	private final int player;
	private boolean prune_check = true;
	private int prunIter;
	private int prunAct;
	private int totalIterations;

	private static final Logger logger = LoggerFactory.getLogger(Node.class);

	public Node(GameAction incomingAction, int player) {
		this.incomingAction = incomingAction;
		this.player = player;
	}

	public boolean canFurtherExpanded() {
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

	private int flipBit(int value){ return value == 0 ? 1 : 0; }

	private void callPruningNets(int actions, int mctsIterations, int treeReuseIterations){
		storeActions(actions, "behaviour/models/actions.csv", false);
		storeActions(mctsIterations, "behaviour/models/mctsIterations.csv", false); //storeActions is used to store iterations here
		storeActions(treeReuseIterations, "behaviour/models/treeReuseIter.csv", false);

		JsonReader jreader = new JsonReader();
		try {
			if (treeReuseIterations >0)
				this.prunIter = jreader.read_iter()-this.getVisits();
			else
				this.prunIter += jreader.read_iter();
			this.prunAct = jreader.read_actions_to_keep();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void prune(Node current, boolean normalizeVisits){
		Map<Node, Integer> map = new HashMap<>();
		for (Node child : current.getChildren())
			map.put(child, child.getVisits());

		Map<Node, Integer> sortedMap = map.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByValue())
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue,
						(oldValue, newValue) -> oldValue, LinkedHashMap::new));

		// temp contains actions to be pruned
		// so we remove from temp the good ones
		List<Node> temp = new LinkedList<>(sortedMap.keySet());
		int actionsToKeep= this.prunAct;

		for (int i=0; i<actionsToKeep; i++)
			temp.remove(temp.size()-1);

		current.getChildren().removeAll(temp);

		// after pruning, set parent visits equal to the sum of the remaining children's visits
		if (normalizeVisits) {
			int parentVisits = 0;
			for (Node child : current.getChildren())
				parentVisits += child.getVisits();
			this.setVisits(parentVisits);
		}
	}

	public void process(ITreePolicy treePolicy, boolean iterativePruning,
						boolean normalizeVisits, int iter, int totalIterations) {
		List<Node> visited = new LinkedList<Node>();
		Node current = this;
		visited.add(this);
		if (iter==0) {
			this.totalIterations = totalIterations;
			// in case of tree reuse
			if (this.getVisits()>0){
				int actions = this.validTransitions.size() + this.getChildren().size();
				callPruningNets(actions, this.totalIterations, this.getVisits());
				this.prune_check = false;
			}
		}

		if (this.prune_check) {
			// get best iterations and actions for pruning from nn
			int actions = this.validTransitions.size() + this.getChildren().size();
			callPruningNets(actions, this.totalIterations, 0);
			this.prune_check = false;
		}

		if (iter==this.prunIter && this.getChildren().size() > 2){
			prune(current, normalizeVisits);
			if (iterativePruning) {
				this.prune_check = true;
				this.totalIterations = totalIterations-iter;
			}
		}

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

	public void setVisits(int visits){ this.visits = visits; }

	private void storeActions(int actions, String filename, boolean append) {
		CsvHandler writer = new CsvHandler();
		StringBuilder data = new StringBuilder();
		data.append(actions);
		if (append) {
			data.append(",");
			data.append(this.getState().getTurn());
			data.append("\n");
		}
		writer.writeToCsv(filename, data, append);
		data.setLength(0);
	}

	private void updateStats(int value) {
		visits++;
		int father;
		int currentPlayer = getState().getActivePlayerId();
		if(incomingAction != null && incomingAction.getActionType().equals(ActionType.END_TURN))
			father = flipBit(currentPlayer);
		else
			father = currentPlayer;

		// if opponent's turn, reverse the value
		if (father == getPlayer())
			score += value;
		else
			score += flipBit(value);
	}

}
