package info.chgk.london;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
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

        ClassPathResource resource = new ClassPathResource(fileName);

        try (FileReader reader = new FileReader(resource.getFile())) {
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();

            for (JsonElement element : jsonArray) {
                ids.add(element.getAsInt());
            }
        }

        return ids;
    }
}