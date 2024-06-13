package org.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LeetCodeProfileScraper {
    public final static Map<String, Integer> USERS_AND_START_POSITION = Map.of(
            "AlexYushchenko", 29,
            "maffic", 25,
            "fisheralexandr", 12,
            "lysykh", 0,
            "sergedmitr", 0,
            "elizavetaMenshikova", 0,
            "bahmytov23", 1,
            "Neroben", 12,
            "gibkinyuriy", 1,
            "DsJava", 4
    );


    public static void main(String[] args) {
        List<UserResult> results = getUserResults(USERS_AND_START_POSITION.keySet());
        sortResultsByChange(results);
        printUserStats(results);

    }

    private static void sortResultsByChange(List<UserResult> results) {
        results.sort((a, b) -> {
            if (a.change.equals("N/A") || b.change.equals("N/A")) {
                return a.change.equals("N/A") ? 1 : -1;
            }
            return Integer.compare(Integer.parseInt(b.change.toString()), Integer.parseInt(a.change.toString()));
        });
    }

    private static void printUserStats(List<UserResult> results) {
        System.out.printf("%-20s%-20s%-20s\n", "Username", "Solved Problems", "Change");
        System.out.println("-------------------- -------------------- --------------------");
        for (UserResult result : results) {
            System.out.printf("%-20s%-20s%-20s\n", result.username, result.solvedCount, result.change);
        }
    }

    static List<UserResult> getUserResults(Set<String> usernames) {
        List<UserResult> results = new ArrayList<>();
        for (String username : usernames) {
            try {
                // URL для получения информации о пользователе
                String url = "https://leetcode.com/graphql";
                String query = "{\"operationName\":\"getUserProfile\",\"variables\":{\"username\":\"" + username + "\"},\"query\":\"query getUserProfile($username: String!) {\\n  matchedUser(username: $username) {\\n    submitStats {\\n      acSubmissionNum {\\n        difficulty\\n        count\\n      }\\n    }\\n  }\\n}\\n\"}";

                // Установка соединения
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, как Gecko) Chrome/58.0.3029.110 Safari/537.3");
                connection.setDoOutput(true);
                connection.getOutputStream().write(query.getBytes());

                // Чтение ответа
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                JsonObject jsonResponse = JsonParser.parseReader(reader).getAsJsonObject();
                reader.close();

                // Обработка и добавление данных в список результатов
                JsonObject matchedUser = jsonResponse.getAsJsonObject("data").getAsJsonObject("matchedUser");
                if (matchedUser != null) {
                    JsonObject submitStats = matchedUser.getAsJsonObject("submitStats");
                    if (submitStats != null) {
                        int solvedCount = submitStats.getAsJsonArray("acSubmissionNum").get(0).getAsJsonObject().get("count").getAsInt();
                        int startPosition = USERS_AND_START_POSITION.getOrDefault(username, 0);
                        int change = solvedCount - startPosition;
                        results.add(new UserResult(username, solvedCount, change));
                    } else {
                        results.add(new UserResult(username, "No stats", "N/A"));
                    }
                } else {
                    results.add(new UserResult(username, "No user found", "N/A"));
                }

            } catch (IOException e) {
                results.add(new UserResult(username, "Error", "N/A"));
            }
        }
        return results;
    }

    static class UserResult {
        String username;
        Object solvedCount;
        Object change;

        UserResult(String username, Object solvedCount, Object change) {
            this.username = username;
            this.solvedCount = solvedCount;
            this.change = change;
        }
    }
}
