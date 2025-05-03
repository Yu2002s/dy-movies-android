package com.danikula.videocache;

public class M3U8AdRange {
    public M3U8AdRange(int start) {
        this.start = start;
        this.end = start;
    }

    public int length() {
        return end - start;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    private int start;

    private int end;

    private int position;

    @Override
    public String toString() {
        return "M3U8AdRange{" +
                "start=" + start +
                ", end=" + end +
                ", position=" + position +
                '}';
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
