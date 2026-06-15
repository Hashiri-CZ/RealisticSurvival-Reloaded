package cz.hashiri.harshlands.disease.model;

import java.util.List;

/** One disease stage. durationTicks is the untreated time before advancing;
 *  for the terminal stage it is also the ceiling ticksInStage is capped to. */
public record DiseaseStage(long durationTicks, List<SymptomSpec> symptoms) {}
