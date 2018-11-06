package br.edu.ufrgs.inf.bpm.changes.sentenceRealization;

public class CollisionPoints {

    private int startCollisionPoint;
    private int endCollisionPoint;

    public CollisionPoints(int startIndex, int endIndex) {
        this.startCollisionPoint = startIndex;
        this.endCollisionPoint = endIndex;
    }

    public int getStartCollisionPoint() {
        return startCollisionPoint;
    }

    public void setStartCollisionPoint(int startCollisionPoint) {
        this.startCollisionPoint = startCollisionPoint;
    }

    public int getEndCollisionPoint() {
        return endCollisionPoint;
    }

    public void setEndCollisionPoint(int endCollisionPoint) {
        this.endCollisionPoint = endCollisionPoint;
    }

    public boolean hasCollision(int startIndex, int endIndex) {
        boolean dontCollide = endIndex < startCollisionPoint || startIndex > endCollisionPoint;
        return !dontCollide;
    }

}
