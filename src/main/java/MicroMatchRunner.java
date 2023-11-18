import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class MicroMatchRunner {
    public static void main(String[] args) throws IOException {
        ArrayList<Integer> tournamentIDs = readIdsFromConfigJson("tournaments.json");
        ArrayList<Integer> teamIDs = readIdsFromConfigJson("teams.json");
        new MicroMatchCalculation(tournamentIDs, teamIDs).calculateScores();
    }

    private static ArrayList<Integer> readIdsFromConfigJson(String fileName) throws IOException {
        ArrayList<Integer> ids = new ArrayList<>();
        Gson gson = new Gson();

        // Assuming config.path is the base path where JSON files are located
        String filePath = System.getenv("config.path") + "/" + fileName;

        try (FileReader reader = new FileReader(filePath)) {
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();

            for (JsonElement element : jsonArray) {
                ids.add(element.getAsInt());
            }
        }

        return ids;
    }
}