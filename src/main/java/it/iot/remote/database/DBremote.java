package it.iot.remote.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBremote {
    private static Connection connection;

    public DBremote() {
        System.out.println("Instantiating!");
    }

    public static Connection connectDbs() {
        if (connection != null) {
            return connection;
        } else {
            try {
                String url = "jdbc:mysql://localhost/collector";
                String user = "root";
                String password = "PASSWORD";

                connection = DriverManager.getConnection(url, user, password);
                return connection;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public static void insertInfo(int lightid, String address) {
        String insert = "INSERT INTO info (lightid,address) VALUES (?, ?, ?)";
        try (Connection connection = connectDbs()) {
            try (PreparedStatement update = connection.prepareStatement(insert)
            ) {
                update.setInt(1, lightid);
                update.setString(2, address);
                update.executeUpdate();
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }


    public static String getAddressForId(int id) {
        String address = null;
        try (Connection connection = connectDbs()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT address FROM info WHERE id = ?")) {
                statement.setInt(1, id);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    address = resultSet.getString("address");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return address;
    }

    public ArrayList<String> selectAllInfo(){
        ArrayList<String> ret = new ArrayList<>();
        String query = "SELECT * FROM info";
        try (Connection smartPoolConnection = connectDbs()) {
            try (PreparedStatement smartPoolPrepareStat = smartPoolConnection.prepareStatement(query)
            ) {
                // Execute the query
                ResultSet resultSet = smartPoolPrepareStat.executeQuery();
                while (resultSet.next()) {
                    int area_id = resultSet.getInt("lightid");
                    String address = resultSet.getString("address");
                    ArrayList<String> row = new ArrayList<>();
                    row.add(String.valueOf(area_id));
                    row.add(address);
                }
                return ret;

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Integer getWearLevelForId (Integer id){
        String query = "SELECT wearLevel FROM coapalarm WHERE id = ?";
        Integer wearLevel = null;
        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                wearLevel = resultSet.getInt("wearLevel");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wearLevel;
    }

    public static Map<String, Object> getLightInfoById(Integer id) {
        String query = "SELECT ALARM, wearLevel, lightFulimnated " +
                "FROM coapalarm " +
                "JOIN coaplightstatus " +
                "ON coapalarm.id = coaplightstatus.id " +
                "WHERE coaplightstatus.id = ?;";
        Map<String, Object> lightInfo = new HashMap<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                lightInfo.put("ALARM", resultSet.getBoolean("ALARM"));
                lightInfo.put("wearLevel", resultSet.getInt("wearLevel"));
                lightInfo.put("lightFulimnated", resultSet.getBoolean("lightFulimnated"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lightInfo;
    }

    public Integer getMaxWearLevelById(Integer id) {
        String query = "SELECT MAX(wearLevel) AS max_wear_level " +
                "FROM coaplightstatus " +
                "WHERE id = ?;";
        Integer maxWearLevel = null;
        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                maxWearLevel = resultSet.getInt("max_wear_level");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maxWearLevel;
    }


    public List<Map<String, Object>> getLightDataByTimestampRange(String startTimestamp, String endTimestamp) {
        String query = "SELECT ALARM, wearLevel, lightFulimnated, timestamp " +
                "FROM coapalarm " +
                "JOIN coaplightstatus " +
                "ON coapalarm.id = coaplightstatus.id " +
                "WHERE timestamp BETWEEN ? AND ?;";
        List<Map<String, Object>> lightData = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement(query);
            preparedStatement.setString(1, startTimestamp);
            preparedStatement.setString(2, endTimestamp);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Map<String, Object> lightInfo = new HashMap<>();
                lightInfo.put("ALARM", resultSet.getBoolean("ALARM"));
                lightInfo.put("wearLevel", resultSet.getInt("wearLevel"));
                lightInfo.put("lightFulimnated", resultSet.getBoolean("lightFulimnated"));
                lightInfo.put("timestamp", resultSet.getString("timestamp"));
                lightData.add(lightInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lightData;
    }

    public Integer getBrightsById(Integer id) {
        String query = "SELECT brights FROM coapmotion WHERE id = ?;";
        Integer brights = null;
        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                brights = resultSet.getInt("brights");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return brights;
    }

    public Map<String, Object> getWearLevelAndAlarmById(Integer id) {
        String query = "SELECT wearLevel, ALARM FROM coapalarm WHERE id = ?;";
        Map<String, Object> wearLevelAndAlarm = new HashMap<>();
        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                wearLevelAndAlarm.put("wearLevel", resultSet.getInt("wearLevel"));
                wearLevelAndAlarm.put("ALARM", resultSet.getBoolean("ALARM"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wearLevelAndAlarm;
    }



    public static String getLastLightAddress() {
        String query = "SELECT configuration.address " +
                "FROM coapalarm " +
                "JOIN coaplightstatus " +
                "ON coapalarm.id = coaplightstatus.id " +
                "JOIN configuration " +
                "ON coaplightstatus.id = configuration.id " +
                "ORDER BY coaplightstatus.id DESC LIMIT 1;";
        String address = null;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                address = resultSet.getString("address");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return address;
    }

    public static String getLastLight() {
        String query = "SELECT light FROM coaplightstatus ORDER BY id DESC LIMIT 1;";
        String light = null;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                light = resultSet.getString("light");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return light;
    }

    public Map<String, Object> getLightBrightWearLevelById(Integer id) {
        String query = "SELECT light, bright, wearLevel FROM coaplightstatus JOIN coapalarm ON coaplightstatus.id = coapalarm.id WHERE coaplightstatus.id = ?;";
        Map<String, Object> lightBrightWearLevel = new HashMap<>();
        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                lightBrightWearLevel.put("light", resultSet.getInt("light"));
                lightBrightWearLevel.put("bright", resultSet.getInt("bright"));
                lightBrightWearLevel.put("wearLevel", resultSet.getInt("wearLevel"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lightBrightWearLevel;
    }
}


