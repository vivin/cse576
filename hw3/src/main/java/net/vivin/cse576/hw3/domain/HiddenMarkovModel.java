package net.vivin.cse576.hw3.domain;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HiddenMarkovModel {

    private State START = new State(SpecialPartOfSpeechTag.Start);
    private State FINAL = new State(SpecialPartOfSpeechTag.Final);

    private Map<PartOfSpeechTag, State> partOfSpeechTagToState;

    public HiddenMarkovModel() {
        partOfSpeechTagToState = Arrays.stream(PartOfSpeechTag.values()).collect(Collectors.toMap(
            Function.identity(),
            State::new
        ));
    }

    public void addStartWord(TaggedWord word) {
        State state = partOfSpeechTagToState.get(word.getPartOfSpeechTag());
        state.addObservation(word.getWord());

        START.addTransitionTo(state);
    }

    public void addBigram(TaggedWord first, TaggedWord second) {
        State preceding = partOfSpeechTagToState.get(first.getPartOfSpeechTag());

        State current = partOfSpeechTagToState.get(second.getPartOfSpeechTag());
        current.addObservation(second.getWord());

        preceding.addTransitionTo(current);
    }

    public void addFinalWord(TaggedWord word) {
        State state = partOfSpeechTagToState.get(word.getPartOfSpeechTag());
        state.addTransitionTo(FINAL);
    }

    public BigDecimal transitionProbabilityFromStartTo(PartOfSpeechTag current) {
        State currentState = partOfSpeechTagToState.get(current);
        return  START.getTransitionProbabilityTo(currentState);
    }

    public BigDecimal transitionProbabilityBetween(PartOfSpeechTag preceding, PartOfSpeechTag current) {
        State precedingState = partOfSpeechTagToState.get(preceding);
        State currentState = partOfSpeechTagToState.get(current);

        return precedingState.getTransitionProbabilityTo(currentState);
    }

    public BigDecimal transitionProbabilityToFinalFrom(PartOfSpeechTag current) {
        State currentState = partOfSpeechTagToState.get(current);
        return currentState.getTransitionProbabilityTo(FINAL);
    }

    public BigDecimal observationProbabilityFor(PartOfSpeechTag tag, String word) {
        State state = partOfSpeechTagToState.get(tag);
        return state.getObservationProbabilityOf(word);
    }

    public List<TaggedWord> tag(List<String> observations) {
        return new Viterbi(observations).tag();
    }

    class Viterbi {

        private List<String> observations;
        private List<Map<PartOfSpeechTag, ScoreAndBackPointer>> scoresAndBackPointers;

        Viterbi(List<String> observations) {
            this.observations = observations;
            this.scoresAndBackPointers = IntStream.range(0, observations.size()).mapToObj(HashMap<PartOfSpeechTag, ScoreAndBackPointer>::new).collect(Collectors.toList());
        }

        public List<TaggedWord> tag() {
            IntStream.range(0, observations.size()).forEach(t -> {
                if(t == 0) {
                    Arrays.stream(PartOfSpeechTag.values()).forEach(j ->
                        scoresAndBackPointers.get(t).put(
                            j, new ScoreAndBackPointer(null, null, transitionProbabilityFromStartTo(j).multiply(observationProbabilityFor(j, observations.get(t))))
                        )
                    );
                } else {
                    Arrays.stream(PartOfSpeechTag.values()).forEach(j ->
                        scoresAndBackPointers.get(t).put(
                            j, Arrays.stream(PartOfSpeechTag.values()).map(i ->
                                    new ScoreAndBackPointer(i, observations.get(t - 1), scoresAndBackPointers.get(t - 1).get(i).score.multiply(transitionProbabilityBetween(i, j)).multiply(observationProbabilityFor(j, observations.get(t))))
                            ).max((s1, s2) -> s1.score.compareTo(s2.score)).get()
                        )
                    );
                }
            });

            ScoreAndBackPointer terminal = Arrays.stream(PartOfSpeechTag.values()).map(i ->
                new ScoreAndBackPointer(i, observations.get(observations.size() - 1), scoresAndBackPointers.get(observations.size() - 1).get(i).score.multiply(transitionProbabilityToFinalFrom(i)))
            ).max((s1, s2) -> s1.score.compareTo(s2.score)).get();

            List<TaggedWord> path = IntStream.iterate(observations.size() - 1, i -> i - 1).limit(observations.size() - 1).mapToObj(i ->
                    new TaggedWord(scoresAndBackPointers.get(i).get(terminal.previous).previous, scoresAndBackPointers.get(i).get(terminal.previous).previousWord)
            ).collect(Collectors.toList());
            Collections.reverse(path);
            path.add(new TaggedWord(terminal.previous, observations.get(observations.size() - 1)));

            return path;
        }

        private class ScoreAndBackPointer {
            private PartOfSpeechTag previous;
            private String previousWord;
            private BigDecimal score;

            public ScoreAndBackPointer(PartOfSpeechTag previous, String previousWord, BigDecimal score) {
                this.previous = previous;
                this.previousWord = previousWord;
                this.score = score;
            }
        }
    }
}
