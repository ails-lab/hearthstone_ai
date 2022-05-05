package net.demilich.metastone.game.behaviour.mctsxgbpruningnets;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MonteCarloTreeSearchXGBPruningNets extends Behaviour {

    private final static Logger logger = LoggerFactory.getLogger(MonteCarloTreeSearchXGBPruningNets.class);

    private int iterations;

    private Node reuseRoot = null;

    private int earlySimTurn;
    private boolean combinedScore;
    private boolean sle;
    private boolean treeReuse;
    private boolean iterativePruning;
    private boolean normalizeVisits;

    public MonteCarloTreeSearchXGBPruningNets(int iterations, int earlySimTurn, boolean combinedScore, boolean sle,
                                              boolean treeReuse, boolean iterativePruning, boolean normalizeVisits) {
        this.iterations = iterations;
        this.earlySimTurn = earlySimTurn;
        this.combinedScore = combinedScore;
        this.sle = sle;
        this.treeReuse = treeReuse;
        this.iterativePruning = iterativePruning;
        this.normalizeVisits = normalizeVisits;
    }

    @Override
    public String getName() { return "MonteCarloTreeSearchXGBPruningNets (" + this.iterations + ", " + this.earlySimTurn + ", " + this.combinedScore + ", " +
            this.sle + ", " + this.treeReuse + ", "  + this.iterativePruning + ", " + this.normalizeVisits + ")"; }

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
            reuseRoot = null;
            return validActions.get(0);
        }
        Node root = new Node(null, player.getId());
        /**-------------------------------------------------*/

        if (!this.treeReuse || reuseRoot == null || !canReuseRoot(validActions, reuseRoot))
            root.rootInitState(context, validActions);
        else
            root = reuseRoot;

        /**-------------------------------------------------*/
        UctPolicy treePolicy = new UctPolicy();
        for (int i = 0; i < iterations; i++) {
            //logger.info("--------  iteration {} ---------", i);
            root.process(treePolicy, earlySimTurn, combinedScore, sle,
                    iterativePruning, normalizeVisits, i, iterations);
        }
        GameAction bestAction = root.getBestAction();

        if(this.treeReuse) {
            if (bestAction.getActionType().equals(ActionType.END_TURN)) {
                reuseRoot = null;
            } else {
                for (Node child : root.getChildren())
                    if (child.getIncomingAction().equals(bestAction)) {
                        reuseRoot = child;
                        break;
                    }
            }
        }
        return bestAction;
    }

    // in case of a battlecry action or a draw card action (e.g. warlock hero power)
    // the real valid actions may differ from the reuse root possible actions
    private boolean canReuseRoot(List<GameAction> validActions, Node reuseRoot) {
        //convert to list of strings in order to compare them
        List<String> validActionsStr = new ArrayList<>();
        List<String> reuseRootPossibleActionsStr = new ArrayList<>();

        for (GameAction action: validActions)
            validActionsStr.add(action.toString());
        for (GameAction action: reuseRoot.getPossibleActions())
            reuseRootPossibleActionsStr.add(action.toString());
        if (new HashSet<>(validActionsStr).equals(new HashSet<>(reuseRootPossibleActionsStr)))
            return true;

        return false;
    }

}
