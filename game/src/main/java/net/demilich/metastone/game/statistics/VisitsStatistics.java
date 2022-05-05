package net.demilich.metastone.game.statistics;

import net.demilich.metastone.game.collect.data.CsvHandler;

import java.util.List;

public class VisitsStatistics {

    public VisitsStatistics(){}

    public static void storeChildrenVisits(String filename, net.demilich.metastone.game.behaviour.mctsxgbpruningnets.Node node){
        CsvHandler writer = new CsvHandler();
        StringBuilder data = new StringBuilder();
        List<net.demilich.metastone.game.behaviour.mctsxgbpruningnets.Node> children = node.getChildren();
        for (int i=0; i<children.size(); i++){
            net.demilich.metastone.game.behaviour.mctsxgbpruningnets.Node child = children.get(i);
            data.append(child.getVisits());
            data.append(",");
        }
        data.append(node.getVisits());
        data.append("\n");
        writer.writeToCsv(filename, data, true);
    }

    public static void storeChildrenVisits(String filename, net.demilich.metastone.game.behaviour.mctsxgb.Node node){
        CsvHandler writer = new CsvHandler();
        StringBuilder data = new StringBuilder();
        List<net.demilich.metastone.game.behaviour.mctsxgb.Node> children = node.getChildren();
        for (int i=0; i<children.size(); i++){
            net.demilich.metastone.game.behaviour.mctsxgb.Node child = children.get(i);
            data.append(child.getVisits());
            data.append(",");
        }
        data.append(node.getVisits());
        data.append("\n");
        writer.writeToCsv(filename, data, true);
    }

    public static void storePrunedVisits(String filename, List<net.demilich.metastone.game.behaviour.mctsxgbpruningnets.Node> pruned){
        CsvHandler writer = new CsvHandler();
        StringBuilder data = new StringBuilder();
        if (pruned !=null) {
            for(int i=0; i<pruned.size(); i++){
                net.demilich.metastone.game.behaviour.mctsxgbpruningnets.Node child = pruned.get(i);
                data.append(child.getVisits());
                data.append(",");
            }
            writer.writeToCsv(filename, data, true);
        }
    }
}