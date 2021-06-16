package de.uos.informatik.ko.rcp;

public class Config {
    public static enum Crossover {
        ONE_POINT,
        TWO_POINT
    }

    public static enum ParentSelection {
        FIXED_SIZE,
        RANDOM_SIZE
    }

    static public void init(Crossover crossover, double mutationProbability,
                            int noImprovementThreshold, ParentSelection parentSelection,
                            boolean cacheMakespans, boolean shouldLog) {
        if (Config.instance != null) {
            throw new IllegalStateException("Config.init() called more than once");
        }

        Config.instance = new Config(crossover, mutationProbability, noImprovementThreshold,
                                     parentSelection, cacheMakespans, shouldLog);
    }

    static public Config instance() {
        if (Config.instance == null) {
            throw new IllegalStateException("Config.init() must be called before " +
                                            "Config.instance()");
        }

        return Config.instance;
    }

    private Config(Crossover crossover, double mutationProbability, int noImprovementThreshold,
                   ParentSelection parentSelection, boolean cacheMakespans, boolean shouldLog) {
        this.crossover = crossover;
        this.mutationProbability = mutationProbability;
        this.noImprovementThreshold = noImprovementThreshold;
        this.parentSelection = parentSelection;
        this.cacheMakespans = cacheMakespans;
        this.shouldLog = shouldLog;
    }

    static private Config instance = null;
    public final Crossover crossover;
    public final double mutationProbability;
    public final int noImprovementThreshold;
    public final ParentSelection parentSelection;
    public final boolean cacheMakespans;
    public final boolean shouldLog;
}
