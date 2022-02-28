package Extra;

import java.sql.*;

public class testDB {

    private static final Object TAG = "testDB";

    public static void main(String[] args) throws SQLException {
        Connection con = null;
        String url = "jdbc:mariadb://localhost:3306/pokemonDB";
        String user = "root";
        String pwd = "";//"ZsTBHYS48kZNpV11tZuf";
        con = DriverManager.getConnection(url, user, pwd);
        System.out.println("connected sucesfully");
        try (Statement statement = con.createStatement()){
//            System.out.println(rs.getRef(1));
            statement.execute("""
                    insert into User (name) value ('Welt2');
                    """);

//            if (rs.first()) {
//                do {
                    // ---START--- GENERATE THIS CODE AUTOMATICALLY
//                    System.out.println(rs.getObject("Bezeichnung"));
//                    System.out.println( rs.findColumn("abc"));
                    // ----END---- GENERATE THIS CODE AUTOMATICALLY
//                } while (rs.next());
//            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
