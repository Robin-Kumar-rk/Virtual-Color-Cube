package com.colorcube.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.colorcube.model.Face;
import com.colorcube.model.Move;

public class Scrambler {
    private static final Random random = new Random();
    private static final Face[] FACES = Face.values();

    public static List<Move> generateScramble(int length) {
        List<Move> scramble = new ArrayList<>();
        Face prevFace = null;

        for (int i = 0; i < length; i++) {
            Face face;
            do {
                face = FACES[random.nextInt(FACES.length)];
            } while (face == prevFace); // Simple avoidance of immediate repeat

            String notation = face.toString();

            Move move = Move.fromString(notation);
            if (move != null) {
                scramble.add(move);
                prevFace = face;
            }
        }
        return scramble;
    }
}
