package com.colorcube.model;

public enum Move {
    U("U", Face.U, 1), U_PRIME("U'", Face.U, -1), U2("U2", Face.U, 2),
    R("R", Face.R, 1), R_PRIME("R'", Face.R, -1), R2("R2", Face.R, 2),
    F("F", Face.F, 1), F_PRIME("F'", Face.F, -1), F2("F2", Face.F, 2),
    D("D", Face.D, 1), D_PRIME("D'", Face.D, -1), D2("D2", Face.D, 2),
    L("L", Face.L, 1), L_PRIME("L'", Face.L, -1), L2("L2", Face.L, 2),
    B("B", Face.B, 1), B_PRIME("B'", Face.B, -1), B2("B2", Face.B, 2);

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
