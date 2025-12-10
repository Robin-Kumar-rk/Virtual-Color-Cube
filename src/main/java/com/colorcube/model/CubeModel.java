package com.colorcube.model;

import java.util.Arrays;
import java.util.Stack;

public class CubeModel {
    // 54 facelets. Order: U1-U9, R1-R9, F1-F9, D1-D9, L1-L9, B1-B9
    // U: 0-8, R: 9-17, F: 18-26, D: 27-35, L: 36-44, B: 45-53
    private char[] facelets;
    private final Stack<Move> moveHistory;

    // Solved state colors (standard scheme)
    // U=White(W), R=Red(R), F=Green(G), D=Yellow(Y), L=Orange(O), B=Blue(B)
    private static final String SOLVED_STATE = "WWWWWWWWW" + "RRRRRRRRR" + "GGGGGGGGG" +
            "YYYYYYYYY" + "OOOOOOOOO" + "BBBBBBBBB";

    public CubeModel() {
        this.facelets = new char[54];
        this.moveHistory = new Stack<>();
        reset();
    }

    public void reset() {
        setFacelets(SOLVED_STATE);
        moveHistory.clear();
    }

    public void setFacelets(String state) {
        if (state.length() != 54) {
            throw new IllegalArgumentException("State must be 54 characters");
        }
        this.facelets = state.toCharArray();
    }

    public String getFaceletString() {
        return new String(facelets);
    }

    public void applyMove(Move move) {
        performRotation(move);
        moveHistory.push(move);
    }

    public void undo() {
        if (!moveHistory.isEmpty()) {
            Move move = moveHistory.pop();
            rotateFaceCounterClockwise(move.getFace());
        }
    }

    public Move getLastMove() {
        if (moveHistory.isEmpty())
            return null;
        return moveHistory.peek();
    }

    // Core rotation logic
    private void performRotation(Move move) {
        // Always 1 clockwise turn
        rotateFaceClockwise(move.getFace());
    }

    private void rotateFaceCounterClockwise(Face face) {
        rotateFaceSurfaceCCW(face);
        rotateAdjacentLayersCCW(face);
    }

    private void rotateFaceSurfaceCCW(Face face) {
        int offset = face.ordinal() * 9;
        char[] temp = Arrays.copyOfRange(facelets, offset, offset + 9);
        // CCW mapping:
        // 0 <- 2, 1 <- 5, 2 <- 8
        // 3 <- 1, 4 <- 4, 5 <- 7
        // 6 <- 0, 7 <- 3, 8 <- 6
        facelets[offset + 0] = temp[2];
        facelets[offset + 1] = temp[5];
        facelets[offset + 2] = temp[8];
        facelets[offset + 3] = temp[1];
        facelets[offset + 4] = temp[4];
        facelets[offset + 5] = temp[7];
        facelets[offset + 6] = temp[0];
        facelets[offset + 7] = temp[3];
        facelets[offset + 8] = temp[6];
    }

    private void rotateAdjacentLayersCCW(Face face) {
        int[][] adj = getAdjacencyMap(face);
        char[] temp = new char[3];

        // Save adj[0]
        for (int i = 0; i < 3; i++)
            temp[i] = facelets[adj[0][i]];

        // adj[0] = adj[3]
        for (int i = 0; i < 3; i++)
            facelets[adj[0][i]] = facelets[adj[3][i]];

        // adj[3] = adj[2]
        for (int i = 0; i < 3; i++)
            facelets[adj[3][i]] = facelets[adj[2][i]];

        // adj[2] = adj[1]
        for (int i = 0; i < 3; i++)
            facelets[adj[2][i]] = facelets[adj[1][i]];

        // adj[1] = temp
        for (int i = 0; i < 3; i++)
            facelets[adj[1][i]] = temp[i];
    }

    private int[][] getAdjacencyMap(Face face) {
        switch (face) {
            case U:
                return new int[][] { { 18, 19, 20 }, { 9, 10, 11 }, { 45, 46, 47 }, { 36, 37, 38 } };
            case D:
                return new int[][] { { 24, 25, 26 }, { 42, 43, 44 }, { 51, 52, 53 }, { 15, 16, 17 } };
            case L:
                return new int[][] { { 0, 3, 6 }, { 53, 50, 47 }, { 27, 30, 33 }, { 18, 21, 24 } };
            case R:
                return new int[][] { { 2, 5, 8 }, { 20, 23, 26 }, { 29, 32, 35 }, { 51, 48, 45 } };
            case F:
                return new int[][] { { 6, 7, 8 }, { 38, 41, 44 }, { 27, 28, 29 }, { 15, 12, 9 } };
            case B:
                return new int[][] { { 2, 1, 0 }, { 11, 14, 17 }, { 33, 34, 35 }, { 42, 39, 36 } };
            default:
                throw new IllegalArgumentException("Unknown face: " + face);
        }
    }

    private void rotateFaceClockwise(Face face) {
        // 1. Rotate the face stickers themselves
        rotateFaceSurface(face);
        // 2. Rotate the adjacent layers
        rotateAdjacentLayers(face);
    }

    private void rotateFaceSurface(Face face) {
        int offset = face.ordinal() * 9;
        char[] temp = Arrays.copyOfRange(facelets, offset, offset + 9);
        // 0 1 2 6 3 0
        // 3 4 5 -> 7 4 1
        // 6 7 8 8 5 2
        facelets[offset + 0] = temp[6];
        facelets[offset + 1] = temp[3];
        facelets[offset + 2] = temp[0];
        facelets[offset + 3] = temp[7];
        facelets[offset + 4] = temp[4]; // Center unchanged
        facelets[offset + 5] = temp[1];
        facelets[offset + 6] = temp[8];
        facelets[offset + 7] = temp[5];
        facelets[offset + 8] = temp[2];
    }

    private void rotateAdjacentLayers(Face face) {
        int[][] adj = getAdjacencyMap(face);
        char[] temp = new char[3];
        // Save adj[0]
        for (int i = 0; i < 3; i++)
            temp[i] = facelets[adj[0][i]];

        // adj[0] = adj[1]
        for (int i = 0; i < 3; i++) {
            facelets[adj[0][i]] = facelets[adj[1][i]];
        }

        // adj[1] = adj[2]
        for (int i = 0; i < 3; i++)
            facelets[adj[1][i]] = facelets[adj[2][i]];

        // adj[2] = adj[3]
        for (int i = 0; i < 3; i++)
            facelets[adj[2][i]] = facelets[adj[3][i]];

        // adj[3] = temp
        for (int i = 0; i < 3; i++)
            facelets[adj[3][i]] = temp[i];
    }
}
