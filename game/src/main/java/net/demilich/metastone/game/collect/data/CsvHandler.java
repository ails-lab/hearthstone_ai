package net.demilich.metastone.game.collect.data;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CsvHandler {

    String currentPath;

    public CsvHandler(){
        currentPath = Paths.get("").toAbsolutePath().toString() + "/game/src/main/java/net/demilich/metastone/game/";
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath += currentPath;
    }

    public int readCsv(String filename){
        String csvFile = Paths.get("").toAbsolutePath().toString() + "/game/src/main/java/net/demilich/metastone/game/behaviour/mctsxgb/" + filename;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            int c = br.read();
            if(c == 48) // ascii 48->0
                return 0;
            else if (c == 49) // ascii 49->1
                return 1;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public double[][] readCsvArray(String filename){
        String csvFile = Paths.get("").toAbsolutePath().toString() + "/game/src/main/java/net/demilich/metastone/game/behaviour/mctsxgb/" + filename;

        // first read csv to a list of lists
        List<List<Double>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                List<Double> row = new ArrayList<>();
                for(int j=0; j<values.length; j++){
                    double d=Double.parseDouble(values[j]);
                    row.add(d);

                }
                records.add(row);
            }

            //convert to 2d array
            double[][] array = new double[records.size()][records.get(0).size()];
            for (int i=0; i<records.size(); i++)
                for (int j=0; j<records.get(0).size(); j++)
                    array[i][j] = records.get(i).get(j);
            return array;
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public void storeLabelsToCsv(String filename, String filename2, int winnerId){
        try (BufferedReader br = new BufferedReader(new FileReader(currentPath + filename))) {
            int c;
            while ((c = br.read()) != -1) {
                if (c == 48 || c == 49) {  // ascii 48->0, 49->1
                    if ((char) c == (char) winnerId+48) // active player won
                        writeToCsv(filename2, new StringBuilder("1\n"), true);
                    else    // active player lost
                        writeToCsv(filename2, new StringBuilder("0\n"), true);
                }
            }
            writeToCsv(filename, new StringBuilder(), false);
        } catch (FileNotFoundException e){
            System.out.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToCsv(String filename, StringBuilder sb, boolean append) {
        try (FileWriter writer = new FileWriter(new File(currentPath + filename), append)) {
            writer.write(sb.toString());
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}