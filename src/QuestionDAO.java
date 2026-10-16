import java.sql.*;
import java.util.*;

/** DAO: all JDBC code for the questions table. */
public class QuestionDAO {

    // category, question, correct answer (always first), wrong1, wrong2, wrong3
    private static final String[][] SEED = {
        {"Java", "Which keyword is used to inherit a class in Java?", "extends", "implements", "inherits", "super"},
        {"Java", "What does JVM stand for?", "Java Virtual Machine", "Java Variable Method", "Joint Virtual Memory", "Java Verified Module"},
        {"Java", "Which collection does NOT allow duplicate elements?", "Set", "List", "ArrayList", "LinkedList"},
        {"Java", "Default value of an int instance variable is?", "0", "null", "1", "undefined"},
        {"Java", "Which one is NOT a primitive data type?", "String", "int", "boolean", "char"},
        {"Java", "Which JavaFX control is used to show data in rows and columns?", "TableView", "ListView", "ComboBox", "Label"},
        {"DBMS", "Which key uniquely identifies each row in a table?", "Primary key", "Foreign key", "Candidate view", "Alternate index"},
        {"DBMS", "In ACID, the letter 'I' stands for?", "Isolation", "Integrity", "Indexing", "Inheritance"},
        {"DBMS", "Which JOIN returns only the matching rows from both tables?", "INNER JOIN", "LEFT JOIN", "FULL JOIN", "CROSS JOIN"},
        {"DBMS", "Which normal form removes partial dependency?", "2NF", "1NF", "3NF", "BCNF"},
        {"DBMS", "Which SQL command is used to retrieve data?", "SELECT", "INSERT", "UPDATE", "ALTER"},
        {"Aptitude", "Find the next number: 2, 6, 12, 20, ?", "30", "28", "32", "24"},
        {"Aptitude", "What is 20% of 250?", "50", "40", "60", "25"},
        {"Aptitude", "A train runs at 60 km/h. Distance covered in 2.5 hours?", "150 km", "120 km", "140 km", "180 km"},
        {"Aptitude", "Average of 10, 20, 30 and 40 is?", "25", "20", "30", "35"},
        {"Aptitude", "If A is 25% more than B, then B is what % less than A?", "20%", "25%", "15%", "30%"},
        {"CS Basics", "Time complexity of binary search?", "O(log n)", "O(n)", "O(n log n)", "O(1)"},
        {"CS Basics", "Which data structure follows LIFO?", "Stack", "Queue", "Array", "Tree"},
        {"CS Basics", "Which data structure is used in Breadth First Search?", "Queue", "Stack", "Heap", "Graph matrix"},
        {"CS Basics", "Which protocol is used to load web pages?", "HTTP", "FTP", "SMTP", "SSH"},
    };

    public static void seedIfEmpty() {
        if (count() > 0) return;
        String sql = "INSERT INTO questions(category, question, a, b, c, d, correct) VALUES (?,?,?,?,?,?,0)";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (String[] r : SEED) {
                for (int i = 0; i < 6; i++) ps.setString(i + 1, r[i]);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int count() {
        try (Connection c = Db.get(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM questions")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> categories() {
        List<String> list = new ArrayList<>();
        try (Connection c = Db.get(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT DISTINCT category FROM questions ORDER BY category")) {
            while (rs.next()) list.add(rs.getString(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    /** Load random questions; category "All" means every category. */
    public static List<Question> load(String category, int limit) {
        boolean all = category == null || category.equals("All");
        String sql = "SELECT id, category, question, a, b, c, d, correct FROM questions "
                + (all ? "" : "WHERE category = ? ") + "ORDER BY RANDOM() LIMIT ?";
        List<Question> list = new ArrayList<>();
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            int k = 1;
            if (!all) ps.setString(k++, category);
            ps.setInt(k, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String[] opts = {rs.getString("a"), rs.getString("b"), rs.getString("c"), rs.getString("d")};
                    list.add(new Question(rs.getInt("id"), rs.getString("category"),
                            rs.getString("question"), opts, rs.getInt("correct")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
}
