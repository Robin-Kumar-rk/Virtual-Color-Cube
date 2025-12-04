package com.colorcube.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.colorcube.model.Face;
import com.colorcube.model.Move;

public class Scrambler {
    private static final Random random = new Random();
    private static final Face[] FACES = Face.values();
    private static final String[] SUFFIXES = { "", "'", "2" };

    public static List<Move> generateScramble(int length) {
        List<Move> scramble = new ArrayList<>();
        Face prevFace = null;

        for (int i = 0; i < length; i++) {
            Face face;
            do {
                face = FACES[random.nextInt(FACES.length)];
            } while (face == prevFace); // Simple avoidance of immediate repeat

            String suffix = SUFFIXES[random.nextInt(SUFFIXES.length)];
            String notation = face.toString() + suffix;

            Move move = Move.fromString(notation);
            if (move != null) {
                scramble.add(move);
                prevFace = face;
            }
        }
        return scramble;
    }

    public static String getScrambleString(List<Move> scramble) {
        StringBuilder sb = new StringBuilder();
        for (Move m : scramble) {
            sb.append(m.getNotation()).append(" ");
        }
        return sb.toString().trim();
    }
}
