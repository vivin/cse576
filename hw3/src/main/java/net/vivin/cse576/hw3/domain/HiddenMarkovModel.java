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
            int T = observations.size() - 1;
            IntStream.range(0, T + 1).forEach(t -> {
                if (t == 0) {
                    Arrays.stream(PartOfSpeechTag.values()).forEach(j -> {
                        Map<PartOfSpeechTag, ScoreAndBackPointer> vbt_1 = scoresAndBackPointers.get(0);

                        BigDecimal a_0j = transitionProbabilityFromStartTo(j);
                        String o_1 = observations.get(0);
                        BigDecimal b_j_of_o_1 = observationProbabilityFor(j, o_1);
                        ScoreAndBackPointer vbt_1_of_j = new ScoreAndBackPointer(a_0j.multiply(b_j_of_o_1));

                        vbt_1.put(j, vbt_1_of_j);
                    });
                } else {
                    Arrays.stream(PartOfSpeechTag.values()).forEach(j -> {
                        Map<PartOfSpeechTag, ScoreAndBackPointer> vbt_t = scoresAndBackPointers.get(t);

                        ScoreAndBackPointer vbt_t_of_j = Arrays.stream(PartOfSpeechTag.values()).map(i -> {
                            Map<PartOfSpeechTag, ScoreAndBackPointer> vbt_t_minus_1 = scoresAndBackPointers.get(t - 1);

                            BigDecimal vbt_t_minus_1_of_i = vbt_t_minus_1.get(i).score;
                            BigDecimal a_ij = transitionProbabilityBetween(i, j);
                            String o_t = observations.get(t);
                            BigDecimal b_j_of_o_t = observationProbabilityFor(j, o_t);
                            String o_t_minus_1 = observations.get(t - 1);

                            return new ScoreAndBackPointer(i, o_t_minus_1, vbt_t_minus_1_of_i.multiply(a_ij).multiply(b_j_of_o_t));
                        }).max((s1, s2) -> s1.score.compareTo(s2.score)).get();

                        vbt_t.put(j, vbt_t_of_j);
                    });
                }
            });

            ScoreAndBackPointer q_T_star = Arrays.stream(PartOfSpeechTag.values()).map(i -> {
                Map<PartOfSpeechTag, ScoreAndBackPointer> vbt_T = scoresAndBackPointers.get(T);

                BigDecimal vbt_T_of_i = vbt_T.get(i).score;
                BigDecimal a_iF = transitionProbabilityToFinalFrom(i);
                String o_T_minus_1 = observations.get(T - 1);

                return new ScoreAndBackPointer(i, o_T_minus_1, vbt_T_of_i.multiply(a_iF));
            }).max((s1, s2) -> s1.score.compareTo(s2.score)).get();

            List<TaggedWord> path = new ArrayList<>();

            ScoreAndBackPointer q_t = q_T_star;
            int t = T;
            while(t >= 0) {
                path.add(new TaggedWord(q_t.previous, q_t.previousWord));
                q_t = scoresAndBackPointers.get(t).get(q_t.previous);
                t--;
            }

            Collections.reverse(path);
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

            public ScoreAndBackPointer(BigDecimal score) {
                this(null, null, score);
            }
        }
    }
}
