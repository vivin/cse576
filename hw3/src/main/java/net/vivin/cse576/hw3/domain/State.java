package net.vivin.cse576.hw3.domain;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

class State {

    private static BigDecimal UNKNOWN_OBSERVATION_PROBABILITY = new BigDecimal("1e-7");

    private Tag tag;

    private int totalTransitions = 0;
    private int totalObservations = 0;

    private Map<State, Integer> successiveStateCounts = new HashMap<>();
    private Map<String, Integer> observationCounts = new HashMap<>();

    public State(Tag tag) {
        this.tag = tag;
    }

    public Tag getTag() {
        return tag;
    }

    public void addTransitionTo(State state) {
        successiveStateCounts.put(state, successiveStateCounts.getOrDefault(state, 0) + 1);
        totalTransitions++;
    }

    public void addObservation(String observation) {
        observationCounts.put(observation, observationCounts.getOrDefault(observation, 0) + 1);
        totalObservations++;
    }

    public BigDecimal getTransitionProbabilityTo(State state) {
        int count = successiveStateCounts.getOrDefault(state, 0);
        return new BigDecimal(count).divide(new BigDecimal(totalTransitions), 30, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal getObservationProbabilityOf(String observation) {
        if(observationCounts.get(observation) == null) {
            return UNKNOWN_OBSERVATION_PROBABILITY;
        } else {
            int count = observationCounts.get(observation);
            return new BigDecimal(count).divide(new BigDecimal(totalObservations), 30, BigDecimal.ROUND_HALF_UP)
                .subtract(UNKNOWN_OBSERVATION_PROBABILITY.divide(new BigDecimal(observationCounts.size()), 30, BigDecimal.ROUND_HALF_UP));
        }
    }

    // Both equality and identity are defined on the basis of the tag, since
    // since there will only ever be one State instance per part-of-speech tag.

    @Override
    public boolean equals(Object o) {
        return (o != null) && (o instanceof State) && ((State) o).tag.equals(this.tag);
    }

    @Override
    public int hashCode() {
        return this.tag.hashCode();
    }

    @Override
    public String toString() {
        return this.tag.toString();
    }
}
