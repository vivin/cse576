package net.vivin.cse576.hw3.service;

import net.vivin.cse576.hw3.domain.PartOfSpeechTag;
import net.vivin.cse576.hw3.domain.TaggedWord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TaggedWordSetFileParser {

    public static void parseSentences(InputStream inputStream, Consumer<List<TaggedWord>> consumer) throws IOException {
        List<TaggedWord> taggedWords = new ArrayList<>();

        new BufferedReader(new InputStreamReader(inputStream)).lines().skip(1).forEach(line -> {
            if (line.startsWith("###/###")) {
                consumer.accept(new ArrayList<>(taggedWords));
                taggedWords.clear();
            } else {
                String[] parts = line.split("/");
                taggedWords.add(new TaggedWord(PartOfSpeechTag.fromString(parts[1]), parts[0]));
            }
        });
    }
}
