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
            // Inverse of a clockwise move is 3 clockwise moves
            performRotation(move);
            performRotation(move);
            performRotation(move);
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
        // Indices for the 3 stickers on each of the 4 adjacent faces
        // The array 'adj' stores the indices of the 4 adjacent segments in cyclic
        // order.
        // The rotation logic shifts them: adj[0] <- adj[1] <- adj[2] <- adj[3] <-
        // adj[0]
        // This corresponds to Target <- Source.

        int[][] adj;

        switch (face) {
            case U:
                // U CW: F(Top) -> L(Top) -> B(Top) -> R(Top) -> F(Top)
                // Cycle: F -> L -> B -> R -> F
                // Target gets Source: F gets R. R gets B. B gets L. L gets F.
                // Wait, standard shift logic:
                // temp = adj[0]; adj[0] = adj[1]; ...
                // If adj = {F, R, B, L}, then F=R, R=B, B=L, L=temp(F).
                // This matches F gets R.
                // Indices:
                // F Top: 18, 19, 20
                // R Top: 9, 10, 11
                // B Top: 45, 46, 47
                // L Top: 36, 37, 38
                adj = new int[][] { { 18, 19, 20 }, { 9, 10, 11 }, { 45, 46, 47 }, { 36, 37, 38 } };
                break;

            case D:
                // D CW: F(Bot) -> R(Bot) -> B(Bot) -> L(Bot) -> F(Bot)
                // Cycle: F -> R -> B -> L -> F
                // Target gets Source: F gets L. L gets B. B gets R. R gets F.
                // If adj = {F, L, B, R}, then F=L, L=B, B=R, R=F.
                // Indices:
                // F Bot: 24, 25, 26
                // L Bot: 42, 43, 44
                // B Bot: 51, 52, 53
                // R Bot: 15, 16, 17
                adj = new int[][] { { 24, 25, 26 }, { 42, 43, 44 }, { 51, 52, 53 }, { 15, 16, 17 } };
                break;

            case L:
                // L CW: U(Left) -> F(Left) -> D(Left) -> B(Right) -> U(Left)
                // Cycle: U -> F -> D -> B -> U
                // Target gets Source: U gets B. B gets D. D gets F. F gets U.
                // If adj = {U, B, D, F}, then U=B, B=D, D=F, F=U.
                // Indices:
                // U Left: 0, 3, 6
                // B Right: 53, 50, 47 (53=Bottom-Right, 47=Top-Right)
                // D Left: 27, 30, 33
                // F Left: 18, 21, 24
                adj = new int[][] { { 0, 3, 6 }, { 53, 50, 47 }, { 27, 30, 33 }, { 18, 21, 24 } };
                break;

            case R:
                // R CW: U(Right) -> B(Left) -> D(Right) -> F(Right) -> U(Right)
                // Cycle: U -> B -> D -> F -> U
                // Target gets Source: U gets F. F gets D. D gets B. B gets U.
                // If adj = {U, F, D, B}, then U=F, F=D, D=B, B=U.
                // Indices:
                // U Right: 2, 5, 8
                // F Right: 20, 23, 26
                // D Right: 29, 32, 35
                // B Left: 51, 48, 45 (51=Bottom-Left, 45=Top-Left)
                adj = new int[][] { { 2, 5, 8 }, { 20, 23, 26 }, { 29, 32, 35 }, { 51, 48, 45 } };
                break;

            case F:
                // F CW: U(Bot) -> R(Left) -> D(Top) -> L(Right) -> U(Bot)
                // Cycle: U -> R -> D -> L -> U
                // Target gets Source: U gets L. L gets D. D gets R. R gets U.
                // If adj = {U, L, D, R}, then U=L, L=D, D=R, R=U.
                // Indices:
                // U Bot: 6, 7, 8
                // L Right: 38, 41, 44 (38=Top-Right, 44=Bot-Right)
                // D Top: 27, 28, 29
                // R Left: 15, 12, 9 (15=Bot-Left, 9=Top-Left)
                adj = new int[][] { { 6, 7, 8 }, { 38, 41, 44 }, { 27, 28, 29 }, { 15, 12, 9 } };
                break;

            case B:
                // B CW: U(Top) -> L(Left) -> D(Bot) -> R(Right) -> U(Top)
                // Cycle: U -> L -> D -> R -> U
                // Target gets Source: U gets R. R gets D. D gets L. L gets U.
                // If adj = {U, R, D, L}, then U=R, R=D, D=L, L=U.
                // Indices:
                // U Top: 2, 1, 0 (2=Top-Right, 0=Top-Left)
                // R Right: 11, 14, 17 (11=Top-Right, 17=Bot-Right)
                // D Bot: 33, 34, 35 (33=Bot-Left, 35=Bot-Right)
                // L Left: 42, 39, 36 (42=Bot-Left, 36=Top-Left)
                adj = new int[][] { { 2, 1, 0 }, { 11, 14, 17 }, { 33, 34, 35 }, { 42, 39, 36 } };
                break;

            default:
                return;
        }

        char[] temp = new char[3];
        // Save adj[0]
        for (int i = 0; i < 3; i++)
            temp[i] = facelets[adj[0][i]];

        // adj[0] = adj[1]
        for (int i = 0; i < 3; i++)
            facelets[adj[0][i]] = facelets[adj[1][i]];

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
