package cz.hashiri.harshlands.disease.model;

/** Immutable snapshot of one infection's progression state.
 *  stage 0 = incubating; stages 1..n are active stages. */
public record InfectionState(int stage, long ticksInStage, long incubationLeft) {}
