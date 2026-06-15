package cz.hashiri.harshlands.data.disease;

/** Mutable per-player runtime state for one active infection.
 *  stage 0 = incubating; stages 1..n are active. */
public final class ActiveInfection {
    private final String diseaseId;
    private int stage;
    private long ticksInStage;
    private long incubationLeft;
    private final long contractedAt;

    public ActiveInfection(String diseaseId, int stage, long ticksInStage,
                           long incubationLeft, long contractedAt) {
        this.diseaseId = diseaseId;
        this.stage = stage;
        this.ticksInStage = ticksInStage;
        this.incubationLeft = incubationLeft;
        this.contractedAt = contractedAt;
    }

    public String diseaseId()       { return diseaseId; }
    public int getStage()           { return stage; }
    public long getTicksInStage()   { return ticksInStage; }
    public long getIncubationLeft() { return incubationLeft; }
    public long contractedAt()      { return contractedAt; }

    public void setStage(int stage)                { this.stage = stage; }
    public void setTicksInStage(long ticks)        { this.ticksInStage = ticks; }
    public void setIncubationLeft(long incubation) { this.incubationLeft = incubation; }

    public boolean isIncubating() { return stage == 0; }
}
