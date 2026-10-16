import java.sql.*;

/** JDBC connection + table creation (SQLite file: quizbattle.db). */
public class Db {
    static final String URL = "jdbc:sqlite:quizbattle.db";

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void init() {
        try {
            Class.forName("org.sqlite.JDBC");               // Step 1: load driver
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC jar not found. Put it in the lib folder.", e);
        }
        try (Connection c = get(); Statement st = c.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS questions(id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "category TEXT, question TEXT, a TEXT, b TEXT, c TEXT, d TEXT, correct INTEGER)");
            st.execute("CREATE TABLE IF NOT EXISTS scores(id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "p1 TEXT, p2 TEXT, s1 INTEGER, s2 INTEGER, winner TEXT, category TEXT, "
                    + "played_on TEXT DEFAULT CURRENT_TIMESTAMP)");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        QuestionDAO.seedIfEmpty();
    }
}
