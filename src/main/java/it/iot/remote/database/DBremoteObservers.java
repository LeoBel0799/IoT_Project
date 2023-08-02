package it.iot.remote.database;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static it.iot.remote.database.DBremote.connectDbs;

public class DBremoteObservers {


    public DBremoteObservers() {
        System.out.println("[!] Observers connected!");
    }

    public void insertObserverLight(String lightStatus) {
        if (!DButils.tableLightObserverExsists("LightObserver")){
            DButils.createLightObserverTable();
        }
        String insert = "INSERT INTO observerLight (lightStatus,timestamp) VALUES (?, ?)";
        try (Connection conn = connectDbs()) {
            PreparedStatement stmt = conn.prepareStatement(insert);
            stmt.setString(1, lightStatus);
            stmt.setString(2, getFormattedTimestamp());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public  void insertObserverBright(int brightStatus) {
        if (!DButils.tableBrightObserverExsists("BrightObserver")){
            DButils.createBrightObserverTable();
        }
        String insert = "INSERT INTO observerBright (brightStatus,timestamp) VALUES (?, ?)";

        try (Connection conn = connectDbs()) {

            PreparedStatement stmt = conn.prepareStatement(insert);
            stmt.setString(2, getFormattedTimestamp());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<ArrayList<String>> selectAllLightObservers() {

        ArrayList<ArrayList<String>> results = new ArrayList<>();

        String query = "SELECT * FROM observerLight";

        try (Connection conn = connectDbs()) {

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {

                ArrayList<String> row = new ArrayList<>();

                String lightStatus = rs.getString("brightStatus");
                row.add(String.valueOf(lightStatus));

                String timestamp = rs.getString("timestamp");
                row.add(timestamp);

                results.add(row);

            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return results;

    }

    public static ArrayList<ArrayList<String>> selectAllBrightObserver() {

        ArrayList<ArrayList<String>> results = new ArrayList<>();

        String query = "SELECT * FROM observerBright";

        try (Connection conn = connectDbs()) {

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {

                ArrayList<String> row = new ArrayList<>();

                int brightStatus = rs.getInt("brightStatus");
                row.add(String.valueOf(brightStatus));

                String timestamp = rs.getString("timestamp");
                row.add(timestamp);

                results.add(row);

            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return results;

    }

    /*
        Per far visuallizare i risultati, chiamare questi due metodi nel menu dell'operatore
        ArrayList<ArrayList<String>> results = selectAllInfo();

        for(ArrayList<String> row : results) {
          String lightStatus = String.parseString(row.get(0));
          String time = row.get(1);

          // fai qualcosa con questi valori
        }
     */


    private static String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return formatter.format(now);
    }

}
