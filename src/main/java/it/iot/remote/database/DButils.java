package it.iot.remote.database;

import java.sql.*;

import static it.iot.remote.database.DBremote.connectDbs;

public class DButils {

    public DButils() {
        System.out.println("[!] DB Utils ON!");
    }


    static void createLightObserverTable() {
        try(Connection conn = connectDbs()) {

            Statement stmt = conn.createStatement();

            String sql = "CREATE TABLE LightObserver " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " lightStatus VARCHAR(10), " +
                    " timestamp CURRENT_TIMESTAMP";

            stmt.executeUpdate(sql);

            System.out.println("[!] Light observer table created!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static boolean tableLightObserverExsists(String observerLight) {
        try(Connection conn = connectDbs()) {

            DatabaseMetaData dbMetadata = conn.getMetaData();
            ResultSet tables = dbMetadata.getTables(null, null, observerLight, null);

            if(tables.next()) {
                // Tabella esiste
                return true;
            } else {
                // Tabella non esiste
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }


    static void createBrightObserverTable() {
        try(Connection conn = connectDbs()) {

            Statement stmt = conn.createStatement();

            String sql = "CREATE TABLE BrightObserver " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " brightStatus VARCHAR(10), " +
                    " timestamp CURRENT_TIMESTAMP";

            stmt.executeUpdate(sql);

            System.out.println("[!] Bright observer table created!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static boolean tableBrightObserverExsists(String observerBright) {
        try(Connection conn = connectDbs()) {

            DatabaseMetaData dbMetadata = conn.getMetaData();
            ResultSet tables = dbMetadata.getTables(null, null, observerBright, null);

            if(tables.next()) {
                // Tabella esiste
                return true;
            } else {
                // Tabella non esiste
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }
}
