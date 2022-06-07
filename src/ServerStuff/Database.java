package ServerStuff;

import java.sql.*;

/**
 * @author Zwickelstorfer Felix
 * serves as a connection to the database
 */
public class Database {

    /**
     * the connection
     */
    private static Connection con;

    /**
     * starts the connection
     */
    public static void init() {
        String url = "jdbc:mariadb://localhost:3306/pokemonDB";
        String user = "root";
        String pwd = "";//"ZsTBHYS48kZNpV11tZuf";
        try {
            con = DriverManager.getConnection(url, user, pwd);
        } catch (SQLException ignored) {
        }
    }

    /**
     * exectues the statement
     * @return the errormessage (null if there is none)
     */
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

    /**
     * returns the result of the statement
     * @return the resultset (null if error)
     */
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

    /**
     * if the database is coneccted
     */
    public static boolean isConnected() {
        return con != null;
    }
}
