package net.demilich.metastone.game.behaviour.mctspruningnets;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MonteCarloTreeSearchPruningNets extends Behaviour {

	private final static Logger logger = LoggerFactory.getLogger(MonteCarloTreeSearchPruningNets.class);

	private int iterations;
	private boolean iterativePruning;
	private boolean normalizeVisits;

	public MonteCarloTreeSearchPruningNets(int iterations, boolean iterativePruning,
										   boolean normalizeVisits){
		this.iterations = iterations;
		this.iterativePruning = iterativePruning;
		this.normalizeVisits = normalizeVisits;
	}

	@Override
	public String getName() { return "MonteCarloTreeSearchPruningNets(" + this.iterations + ", "
			+ this.iterativePruning + ", " + this.normalizeVisits + ")"; }

	@Override
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		List<Card> discardedCards = new ArrayList<Card>();
		for (Card card : cards) {
			if (card.getBaseManaCost() >= 4) {
				discardedCards.add(card);
			}
		}
		return discardedCards;
	}

	@Override
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		if (validActions.size() == 1) {
			return validActions.get(0);
		}
		Node root = new Node(null, player.getId());
		root.rootInitState(context, validActions);
		UctPolicy treePolicy = new UctPolicy();
		for (int i = 0; i < iterations; i++) {
			root.process(treePolicy, iterativePruning, normalizeVisits, i, iterations);
		}
		GameAction bestAction = root.getBestAction();
		return bestAction;
	}

}
