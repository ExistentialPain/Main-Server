package com.SCI.game.hex;

public class HexCube {
    public HexCube(int q, int r, int s) {
        this.q = q;
        this.r = r;
        this.s = s;
    }

    public static HexCube fromOffsetCoords(HexOffset h) {
        int q = h.getCol() - (h.getRow() + (h.getRow() & 1)) / 2;
        int r = h.getRow();
        int s = -q - r;
        return new HexCube(q, r, s);
    }

    public HexOffset toOffsetCoords(){
        int col = q + (r + (r & 1)) / 2;
        int row = r;
        return new HexOffset(row, col);
    }

    public HexCube add(HexCube other) {
        return new HexCube(q + other.q, r + other.r, s + other.s);
    }

    public HexCube subtract(HexCube other) {
        return new HexCube(q - other.q, r - other.r, s - other.s);
    }

    public HexCube multiply(HexCube other) {
        return new HexCube(q * other.q, r * other.r, s * other.s);
    }

    public HexCube divide(HexCube other) {
        return new HexCube(q / other.q, r / other.r, s / other.s);
    }

    public int getQ() {
        return q;
    }

    public int getR() {
        return r;
    }

    public int getS() {
        return s;
    }

    private final int q, r, s;
}
