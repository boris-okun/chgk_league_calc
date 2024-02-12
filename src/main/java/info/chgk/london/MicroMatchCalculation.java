package info.chgk.london;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MicroMatchCalculation {
    ArrayList<Integer> tournamentIDs;
    ArrayList<Integer> teamIDs;
    ArrayList<teamResults> results;

    ArrayList <RatingExport.TournamentResults> tournamentResults = new ArrayList<>();

    public MicroMatchCalculation(ArrayList<Integer> tournamentIDs, ArrayList<Integer> teamIDs) throws IOException {
        this.tournamentIDs = tournamentIDs;
        this.teamIDs = teamIDs;
        initTournamentsResults(tournamentIDs, teamIDs);
        this.results = getTeamsResults(tournamentIDs, teamIDs);

    }

    public void calculateScores() {
        for (teamResults team : results) {
            int minQuestions = team.questions.getOrDefault(tournamentIDs.get(0), 0);
            for (int i = 1; i < tournamentIDs.size(); i++) {
                int q = team.questions.getOrDefault(tournamentIDs.get(i), 0);
                if (q < minQuestions) {
                    team.tieBreaker += minQuestions;
                    minQuestions = q;
                } else {
                    team.tieBreaker += q;
                }
            }
            for (teamResults team2 : results) {
                if (team == team2) {
                    team.matches.add(new ArrayList<>());
                    continue;
                }
                int points1 = 0;
                int points2 = 0;
                for (int tournamentID : tournamentIDs) {
                    int q1 = team.questions.getOrDefault(tournamentID, 0);
                    int q2 = team2.questions.getOrDefault(tournamentID, 0);
                    if (q1 > q2) {
                        points1++;
                    } else if (q1 < q2) {
                        points2++;
                    }
                }
                if (points1 > points2) {
                    team.points += 2;
                } else if (points1 == points2) {
                    team.points += 1;
                }
                ArrayList match = new ArrayList<Integer>();
                match.add(points1);
                match.add(points2);
                team.matches.add(match);
            }
        }
        for (teamResults team : results) {
            System.out.println(team.name + "\t" + team.points + "\t" + team.tieBreaker);
        }

        System.out.println("");
        System.out.println("====================================");
        System.out.println("");

        System.out.print("\t");
        for (RatingExport.TournamentResults results : tournamentResults) {
            System.out.print(results.name + "\t");
        }
        System.out.println();
        for (teamResults team : results) {
            System.out.print(team.name + "\t");
            for (RatingExport.TournamentResults tournament : tournamentResults) {
                System.out.print(team.questions.getOrDefault(tournament.id, 0) + "\t");
            }
            System.out.println();
        }

        System.out.println();
        System.out.println("====================================");
        System.out.println();

        for (teamResults team : results) {
            System.out.print(team.name + "\t");
        }
        System.out.println();
        for (teamResults team : results) {
            System.out.print(team.name + "\t");
            for (int i = 0; i < team.matches.size(); i++) {
                if (team.matches.get(i).isEmpty()) {
                    System.out.print("X\t");
                } else {
                    System.out.print(team.matches.get(i).get(0) + ":" + team.matches.get(i).get(1) + "\t");
                }
            }
            System.out.println();
        }
    }

    private void initTournamentsResults(ArrayList<Integer> tournamentIDs, ArrayList<Integer> teamIDs) throws IOException {
        tournamentResults = new ArrayList<RatingExport.TournamentResults>();
        for (int tournamentID : tournamentIDs) {
            tournamentResults.add(RatingExport.TournamentResults.init(tournamentID, teamIDs));
        }
    }

        private ArrayList<teamResults> getTeamsResults(ArrayList<Integer> tournamentIDs, ArrayList<Integer> teamIDs) throws IOException {
        ArrayList<teamResults> results = new ArrayList<teamResults>();
        for (int teamID : teamIDs) {
            results.add(new teamResults(teamID, this.tournamentResults));
        }
        return results;
    }

    static public class teamResults {
        int teamID;
        String name;
        HashMap<Integer, Integer> questions;
        int points;
        ArrayList<ArrayList<Integer>> matches;
        int tieBreaker;


        public teamResults(int teamID, ArrayList <RatingExport.TournamentResults> results) throws IOException {
            this.teamID = teamID;
            this.name = getTeamName(teamID);
            this.questions = new HashMap<Integer, Integer>();
            for (RatingExport.TournamentResults result : results) {
                this.questions.put(result.id, result.results.getOrDefault(teamID, 0));
            }
            this.points = 0;
            this.matches = new ArrayList<ArrayList<Integer>>();
            this.tieBreaker = 0;
        }

        private static String getTeamName(int teamID) throws IOException {
            String json = RatingExport.queryAPI("teams", teamID, null, null);
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(json);
            return jsonElement.getAsJsonObject().get("name").getAsString();
        }
    }
}
