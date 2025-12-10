package com.colorcube.model;

public enum Move {
    U(Face.U, 1), U_PRIME(Face.U, -1),
    R(Face.R, 1), R_PRIME(Face.R, -1),
    F(Face.F, 1), F_PRIME(Face.F, -1),
    D(Face.D, 1), D_PRIME(Face.D, -1),
    L(Face.L, 1), L_PRIME(Face.L, -1),
    B(Face.B, 1), B_PRIME(Face.B, -1);

    private final Face face;
    private final int dir; // 1 = clockwise, -1 = counter-clockwise

    Move(Face face, int dir) {
        this.face = face;
        this.dir = dir;
    }

    public String getNotation() {
        return toString();
    }

    public Face getFace() {
        return face;
    }

    public int getDir() {
        return dir;
    }

    @Override
    public String toString() {
        return face.toString() + (dir == -1 ? "'" : "");
    }

    public static Move fromString(String s) {
        if (s == null)
            return null;
        for (Move m : values()) {
            if (m.toString().equals(s))
                return m;
        }
        return null;
    }
}
