package de.iani.cubequest.generation;

import java.util.Arrays;
import java.util.stream.Collectors;

public class DelegatedGenerationData {
    
    public final String dateString;
    public final int questOrdinal;
    public final double difficulty;
    public final long ranSeed;
    
    public DelegatedGenerationData(String dateString, int questOrdinal, double difficulty,
            long ranSeed) {
        this.dateString = dateString;
        this.questOrdinal = questOrdinal;
        this.difficulty = difficulty;
        this.ranSeed = ranSeed;
    }
    
    @Override
    public String toString() {
        return Arrays.asList(this.dateString, this.questOrdinal, this.difficulty, this.ranSeed)
                .stream().map(x -> String.valueOf(x))
                .collect(Collectors.joining(", ", "DelegatedGenerationData[", "]"));
    }
    
}
