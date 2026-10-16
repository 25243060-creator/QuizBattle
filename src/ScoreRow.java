/** Model: one row of the leaderboard. */
public class ScoreRow {
    public final String date, p1, p2, winner, category;
    public final int s1, s2;

    public ScoreRow(String date, String p1, String p2, int s1, int s2, String winner, String category) {
        this.date = date; this.p1 = p1; this.p2 = p2;
        this.s1 = s1; this.s2 = s2; this.winner = winner; this.category = category;
    }
}
