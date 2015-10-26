package net.vivin.cse576.hw3;

import net.vivin.cse576.hw3.domain.HiddenMarkovModel;
import net.vivin.cse576.hw3.domain.TaggedWord;
import net.vivin.cse576.hw3.service.TaggedWordSetFileParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HMMPoSTagger {

    public static void main(String[] args) throws URISyntaxException, IOException {
        System.out.println("CSE 576 Homework 3: HMM Part of Speech Tagger");
        System.out.println("Author: Vivin S. Paliath");
        System.out.println("ASU ID: 1000511521\n");

        System.out.print("Loading training data and building HMM...");

        HiddenMarkovModel hmm = new HiddenMarkovModel();
        TaggedWordSetFileParser.parseSentences(HMMPoSTagger.class.getResourceAsStream("/entrain.txt"), sentence ->
            IntStream.range(0, sentence.size()).forEach(i -> {
                if(i == 0) {
                    hmm.addStartWord(sentence.get(i));
                }

                if(i > 0) {
                    hmm.addBigram(sentence.get(i - 1), sentence.get(i));
                }

                if(i == sentence.size() - 1) {
                    hmm.addFinalWord(sentence.get(i));
                }
            })
        );

        System.out.print("done.\nTesting");

        Map<String, Long> counts = new HashMap<>();
        TaggedWordSetFileParser.parseSentences(HMMPoSTagger.class.getResourceAsStream("/entest.txt"), sentence -> {
            counts.put("totalWords", counts.getOrDefault("totalWords", 0L) + sentence.size());

            List<TaggedWord> tagged = hmm.tag(sentence.stream().map(TaggedWord::getWord).collect(Collectors.toList()));

            long incorrectlyTagged = IntStream.range(0, sentence.size()).filter(i -> sentence.get(i).getPartOfSpeechTag() != tagged.get(i).getPartOfSpeechTag()).count();
            counts.put("incorrectlyTagged", counts.getOrDefault("incorrectlyTagged", 0L) + incorrectlyTagged);

            if(counts.getOrDefault("tagged", 0L) % 10L == 0) {
                System.out.print(".");
            }

            if(counts.containsKey("tagged") && counts.get("tagged") % 100L == 0) {
                System.out.print(counts.get("tagged"));
            }

            counts.put("tagged", counts.getOrDefault("tagged", 0L) + 1L);
        });

        System.out.println("done.\n");
        System.out.println("Incorrectly tagged " + counts.get("incorrectlyTagged") + " words out of a total of " + counts.get("totalWords") + " for an error rate of " + new BigDecimal(counts.get("incorrectlyTagged")).divide(new BigDecimal(counts.get("totalWords")), 4, BigDecimal.ROUND_HALF_UP) + ".");

    }
}
