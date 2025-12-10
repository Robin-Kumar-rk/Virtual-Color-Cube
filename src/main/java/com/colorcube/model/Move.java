package com.colorcube.model;

public enum Move {
    U("U", Face.U, 1), U_PRIME("U'", Face.U, -1),
    R("R", Face.R, 1), R_PRIME("R'", Face.R, -1),
    F("F", Face.F, 1), F_PRIME("F'", Face.F, -1),
    D("D", Face.D, 1), D_PRIME("D'", Face.D, -1),
    L("L", Face.L, 1), L_PRIME("L'", Face.L, -1),
    B("B", Face.B, 1), B_PRIME("B'", Face.B, -1);

    private final String notation;
    private final Face face;
    private final int dir; // 1 = clockwise, -1 = counter-clockwise, 2 = double

    Move(String notation, Face face, int dir) {
        this.notation = notation;
        this.face = face;
        this.dir = dir;
    }

    public String getNotation() {
        return notation;
    }

    public Face getFace() {
        return face;
    }

    public int getDir() {
        return dir;
    }

    @Override
    public String toString() {
        return notation;
    }

    public static Move fromString(String s) {
        for (Move m : values()) {
            if (m.notation.equals(s))
                return m;
        }
        return null;
    }
}
