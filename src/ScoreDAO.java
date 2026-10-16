import java.sql.*;
import java.util.*;

/** DAO: all JDBC code for the scores table. */
public class ScoreDAO {

    public static void save(String p1, String p2, int s1, int s2, String winner, String category) {
        String sql = "INSERT INTO scores(p1, p2, s1, s2, winner, category) VALUES (?,?,?,?,?,?)";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p1); ps.setString(2, p2);
            ps.setInt(3, s1);    ps.setInt(4, s2);
            ps.setString(5, winner); ps.setString(6, category);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int gamesPlayed() {
        return scalarInt("SELECT COUNT(*) FROM scores");
    }

    public static int highScore() {
        return scalarInt("SELECT COALESCE(MAX(CASE WHEN s1 > s2 THEN s1 ELSE s2 END), 0) FROM scores");
    }

    private static int scalarInt(String sql) {
        try (Connection c = Db.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String lastWinner() {
        try (Connection c = Db.get(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT winner FROM scores ORDER BY id DESC LIMIT 1")) {
            return rs.next() ? rs.getString(1) : "-";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<ScoreRow> top(int n) {
        String sql = "SELECT substr(played_on,1,16), p1, p2, s1, s2, winner, category FROM scores "
                + "ORDER BY (CASE WHEN s1 > s2 THEN s1 ELSE s2 END) DESC, id DESC LIMIT ?";
        List<ScoreRow> list = new ArrayList<>();
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, n);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new ScoreRow(rs.getString(1), rs.getString(2), rs.getString(3),
                            rs.getInt(4), rs.getInt(5), rs.getString(6), rs.getString(7)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
}
