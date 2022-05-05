package net.demilich.metastone.game.behaviour.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonReader {
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    public int read() throws IOException, JSONException {
        JSONObject json = readJsonFromUrl("http://localhost:8000/"); //change this
        int winner = json.getInt("winner");
        return winner;
    }

    public int read_iter() throws IOException, JSONException {
        JSONObject json;
        json = readJsonFromUrl("http://localhost:8003/"); //change this
        int iter = json.getInt("best_iter");
        return iter;
    }

    public int read_actions_to_keep() throws IOException, JSONException {
        JSONObject json = readJsonFromUrl("http://localhost:8003/"); //change this
        int actions = json.getInt("actions_to_keep");
        return actions;
    }

}
