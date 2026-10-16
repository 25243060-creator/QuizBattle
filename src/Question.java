import java.util.*;

/** Model: one MCQ question. */
public class Question {
    public final int id;
    public final String category, text;
    public final String[] options;
    public int correct;

    public Question(int id, String category, String text, String[] options, int correct) {
        this.id = id; this.category = category; this.text = text;
        this.options = options; this.correct = correct;
    }

    /** Shuffle the options so the right answer is not always in the same place. */
    public void shuffle() {
        String right = options[correct];
        List<String> l = new ArrayList<>(Arrays.asList(options));
        Collections.shuffle(l);
        for (int i = 0; i < options.length; i++) options[i] = l.get(i);
        correct = l.indexOf(right);
    }
}
