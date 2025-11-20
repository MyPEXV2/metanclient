package relake.render.display.shape;

import java.util.HashMap;
import java.util.Map;

public class Round {
    private static final Map<String, Round> roundMap = new HashMap<>();
    public float LB;
    public float LT;
    public float RB;
    public float RT;

    public static Round zero() {
        return of(0);
    }

    private Round() {
    }

    private Round(float LB, float LT, float RB, float RT) {
        this.LB = LB;
        this.LT = LT;
        this.RB = RB;
        this.RT = RT;
    }

    public static Round of(float LB, float LT, float RB, float RT) {
        String key = LB + "_" + LT + "_" + RB + "_" + RT;
        if (roundMap.containsKey(key)) {
            return roundMap.get(key);
        } else {
            Round round = new Round(LB, LT, RB, RT);
            roundMap.put(key, round);
            return round;
        }
    }

    public static Round of(float round) {
        return of(round, round, round, round);
    }

    public static Round of(Round round) {
        return of(round.LB, round.LT, round.RB, round.RT);
    }

    public Round sub(float LB, float LT, float RB, float RT) {
        this.LB -= LB;
        this.LT -= LT;
        this.RB -= RB;
        this.RT -= RT;
        return this;
    }

    public Round add(float LB, float LT, float RB, float RT) {
        this.LB += LB;
        this.LT += LT;
        this.RB += RB;
        this.RT += RT;
        return this;
    }

    public Round mul(float LB, float LT, float RB, float RT) {
        this.LB *= LB;
        this.LT *= LT;
        this.RB *= RB;
        this.RT *= RT;
        return this;
    }

    public Round set(float LB, float LT, float RB, float RT) {
        this.LB = LB;
        this.LT = LT;
        this.RB = RB;
        this.RT = RT;
        return this;
    }
}
