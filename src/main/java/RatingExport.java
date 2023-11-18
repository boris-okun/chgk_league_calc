import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RatingExport {
    public static HashMap<Integer, Integer> getTournamentResults(int tournamentID, ArrayList<Integer> teamIDs) throws IOException {
        String json = queryAPI("tournaments", tournamentID, "results", null);
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(json);
        HashMap<Integer, Integer>  results = new HashMap<Integer, Integer>();
        if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (JsonElement element : jsonArray) {
                try {
                    int teamID = element.getAsJsonObject().get("team").getAsJsonObject().get("id").getAsInt();
                    if (teamIDs.contains(teamID)) {
                        int questions = element.getAsJsonObject().get("questionsTotal").getAsInt();
                        String teamName = element.getAsJsonObject().get("team").getAsJsonObject().get("name").getAsString();
                        results.put(teamID, questions);
                    }
                }
                catch (Exception e) {
                    System.out.println("Error parsing team results " + tournamentID);
                }
            }
        }
        else System.out.println("Not a JSON object");
        return results;
    }

    public static String queryAPI(String type, int id, String section, Map<String, String> params) throws IOException {
        String url = "https://api.rating.chgk.net/" + type;
        if (id != 0) {
            url += "/" + id;
        }
        if (section != null) {
            url += "/" + section;
        }
        if (params != null) {
            String payload = params.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .reduce((a, b) -> a + "&" + b)
                    .orElse("");
            if (!payload.isEmpty()) {
                url += "?" + payload;
            }
        }
        URL apiUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
            BufferedReader in = new BufferedReader(inputStreamReader);
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = ((BufferedReader) in).readLine()) != null) {
                response.append(inputLine);
            }
            inputStreamReader.close();
            return response.toString();
        } else {
            throw new RuntimeException("Failed to query API: " + responseCode);
        }
    }

    public static class TournamentResults {
        String name;
        int id;
        HashMap<Integer, Integer> results;

        public TournamentResults(String name, int id, HashMap<Integer, Integer> results) throws IOException {
            this.name = name;
            this.id = id;
            this.results = results;
        }

        public static TournamentResults init(int id, ArrayList<Integer> teamIDs) throws IOException {
            return new TournamentResults(getName(id), id, getTournamentResults(id, teamIDs));
        }

        static String getName(int tournamentID) throws IOException {
            String json = queryAPI("tournaments", tournamentID, null, null);
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(json);
            return jsonElement.getAsJsonObject().get("name").getAsString();
        }
    }
}
