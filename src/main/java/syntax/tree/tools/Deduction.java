package syntax.tree.tools;

public class Deduction {

    private RuleInterval from;
    private RuleInterval[] to;

    public Deduction(RuleInterval from, RuleInterval[] to) {
        this.from = from;
        this.to = to;
    }

    public RuleInterval getFrom() {
        return from;
    }

    public RuleInterval[] getTo() {
        return to;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        if (to.length > 0) sb.append(to[0]);
        for (int i = 1; i < to.length; i++) {
            RuleInterval ri = to[i];
            sb.append(", " + ri);
        }

        sb.append("]");
        return from + " -> " + sb.toString();
    }

}
