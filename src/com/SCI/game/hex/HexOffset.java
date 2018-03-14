package com.SCI.game.hex;

import java.util.LinkedList;

public class HexOffset {
    public HexOffset(int col, int row) {
        this.col = col;
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public LinkedList<HexOffset> getHexesWithinRange(int range) {
        LinkedList<HexOffset> hexes = new LinkedList<>();
        for (int dx = -range; dx <= range; ++dx) {
            for (int dy = Math.max(-range, -dx - range); dy <= Math.min(range, -dx + range); ++dy) {
                int dz = -dx - dy;
                HexCube h = new HexCube(dx, dy, dz);
                HexCube uwi = HexCube.fromOffsetCoords(this);
                HexCube owowo = h.add(uwi);
                hexes.add(owowo.toOffsetCoords());
            }
        }
        return hexes;
    }

    private final int row, col;
}
