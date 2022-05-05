package net.demilich.metastone.game.statistics;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.List;

public class SaveActionVisits {

    public SaveActionVisits(){}

    public void writeVisitsToFile(List<Integer> visits, int iteration) throws IOException {
        FileWriter fileWriter = new FileWriter(Paths.get("").toAbsolutePath().toString() +
                "/game/src/main/java/net/demilich/metastone/game/statistics/visits.txt.txt", true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        FileReader filereader = new FileReader(Paths.get("").toAbsolutePath().toString() +
                "/game/src/main/java/net/demilich/metastone/game/statistics/visits.txt.txt");
		/*if (filereader.read() == -1) {
			printWriter.printf("             ");
			for (Node child : parent.getChildren())
				printWriter.printf("%s ", child.getIncomingAction().toString());
			printWriter.printf("\n");
		}*/
        printWriter.printf("Iteration: %d ", iteration);
        for (int visit: visits)
            printWriter.printf("%d ", visit);
        printWriter.printf("\n");
        printWriter.close();
    }
}
