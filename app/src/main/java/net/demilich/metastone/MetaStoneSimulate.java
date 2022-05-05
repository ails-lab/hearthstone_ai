package net.demilich.metastone;

import javafx.application.Application;
import javafx.stage.Stage;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.behaviour.mctspruningnets.MonteCarloTreeSearchPruningNets;
import net.demilich.metastone.game.behaviour.mctsxgb.MonteCarloTreeSearchXGB;
import net.demilich.metastone.game.behaviour.mctsxgbpruningnets.MonteCarloTreeSearchXGBPruningNets;
import net.demilich.metastone.game.behaviour.threat.GameStateValueBehaviour;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.MetaHero;
import net.demilich.metastone.game.gameconfig.GameConfig;
import net.demilich.metastone.game.gameconfig.PlayerConfig;
import net.demilich.metastone.gui.deckbuilder.DeckFormatProxy;
import net.demilich.metastone.gui.deckbuilder.DeckProxy;
import net.demilich.metastone.gui.simulationmode.SimulateGamesCommand;
import net.demilich.metastone.utils.UserHomeMetastone;
import net.demilich.nittygrittymvc.interfaces.INotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MetaStoneSimulate extends Application{


    private static Logger logger = LoggerFactory.getLogger(MetaStoneSimulate.class);

    public static void main(String[] args,int numberOfGames, Deck player1Deck, Behaviour player1Behaviour, Deck player2Deck, Behaviour player2Behaviour){
        //DevCardTools.formatJsons();

        try {
            // ensure that the user home metastone dir exists
            Files.createDirectories(Paths.get(UserHomeMetastone.getPath()));
        } catch (IOException e) {
            logger.error("Trouble creating " +  Paths.get(UserHomeMetastone.getPath()));
            e.printStackTrace();
        }

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        int[] deckFormatIndex = new int[]{0, 0, 0, 0, 0, 0};
        int[] numberOfGames = new int[]{100, 100, 100};
        Behaviour[] player1Behaviour = new Behaviour[]{
                new MonteCarloTreeSearchPruningNets(500, true, false),
                new MonteCarloTreeSearchXGB(500, 5, true, true, true),
                new MonteCarloTreeSearchXGBPruningNets(500, 5, true, true, true, true, false)};
        // check deck indicesbelow
        int[] player1DeckIndex = new int[]{8, 13, 14};
        Behaviour[] player2Behaviour = new Behaviour[]{
                new GameStateValueBehaviour(),
                new GameStateValueBehaviour(),
                new GameStateValueBehaviour()};
        int[] player2DeckIndex = new int[]{8, 13, 14};

        for(int i=0; i<numberOfGames.length; i++) {
            ApplicationFacade facade = (ApplicationFacade) ApplicationFacade.getInstance();
            facade.startUp();
            GameConfig gameConfig = new GameConfig();

            // Load decks and deck formats
            DeckProxy deckProxy = (DeckProxy) facade.retrieveProxy(DeckProxy.NAME);
            facade.sendNotification(GameNotification.LOAD_DECKS);
            List<Deck> decks = deckProxy.getDecks();

            DeckFormatProxy deckFormatProxy = (DeckFormatProxy) facade.retrieveProxy(DeckFormatProxy.NAME);
            facade.sendNotification(GameNotification.LOAD_DECK_FORMATS);
            List<DeckFormat> deckFormats = deckFormatProxy.getDeckFormats();

            // Check possible decks (the order may vary)
//            for (Deck deck: decks)
//                System.out.println(deck.getFilename());
            // decks[8] --> runandguns_shaman__blizzcon_2014_world_championship_finals.json
            // decks[13] --> firebats_hunter__blizzcon_2014_world_championship_finals.json
            // decks[14] --> tareis_warlock__blizzcon_2014_world_championship_finals.json

            // Set player1 deck and behaviour
            PlayerConfig player1Config = new PlayerConfig();
            Deck deck1 = decks.get(player1DeckIndex[i]);
            player1Config.setDeck(deck1);
            //player1Config.setHeroCard((HeroCard) CardCatalogue.getCardById("hero_rexxar"));
            player1Config.setHeroCard(MetaHero.getHeroCard(deck1.getHeroClass()));
            player1Config.setBehaviour(player1Behaviour[i]);
            player1Config.build();

            // Set player2 deck and behaviour
            PlayerConfig player2Config = new PlayerConfig();
            Deck deck2 = decks.get(player2DeckIndex[i]);
            player2Config.setDeck(deck2);
            player2Config.setHeroCard(MetaHero.getHeroCard(deck2.getHeroClass()));
            player2Config.setBehaviour(player2Behaviour[i]);
            player2Config.build();

            // Set gameConfig
            gameConfig.setDeckFormat(deckFormats.get(deckFormatIndex[i]));    // deckFormats[0] --> all.json
            gameConfig.setNumberOfGames(numberOfGames[i]);
            gameConfig.setPlayerConfig1(player1Config);
            gameConfig.setPlayerConfig2(player2Config);

            // Execute simulate command
            SimulateGamesCommand sgc = new SimulateGamesCommand();
            sgc.execute(new INotification<GameNotification>() {
                @Override
                public Object getBody() {
                    return gameConfig;
                }

                @Override
                public GameNotification getId() {
                    return GameNotification.COMMIT_SIMULATIONMODE_CONFIG;
                }
            });
        }

    }

}
