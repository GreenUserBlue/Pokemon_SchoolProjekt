package ServerStuff;

import java.sql.*;

public class Database {

    private static Connection con;

    public static void init() {
        String url = "jdbc:mariadb://localhost:3306/pokemonDB";
        String user = "root";
        String pwd = "";//"ZsTBHYS48kZNpV11tZuf";
        try {
            con = DriverManager.getConnection(url, user, pwd);
        } catch (SQLException ignored) {
        }
    }

    public static String execute(String statement) {
        try {
            if (con == null) init();
            Statement s = con.createStatement();
            s.execute(statement);
        } catch (SQLException e) {
            return e.getMessage();
        }
        return null;
    }

    public static ResultSet get(String statement) {
        try {
            if (con == null) init();
            if (con != null) {
                Statement s = con.createStatement();
                return s.executeQuery(statement);
            }
        } catch (SQLException ignored) {
        }
        return null;
    }

    public static boolean isConnected() {
        return con != null;
    }
}
