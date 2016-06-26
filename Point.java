public class Point {

  private long x;
  private long y;

  public Point(long X, long Y) {
    x = X;
    y = Y;
  }

  public Point(double X, double Y) {
    x = (long)X;
    y = (long)Y;
  }

  public Point(Point other) {
    x = other.getX();
    y = other.getY();
  }

  public long getX() {
    return x;
  }

  public long getY() {
    return y;
  }

  public boolean sameAs(Point P) {
    if ((P.getX() == x) && (P.getY() == y)) {
      return true;
    } else {
      return false;
    }
  }

  public Point plus(Line vector) {
    long deltaX = vector.endPoint.getX() - vector.startPoint.getX();
    long deltaY = vector.endPoint.getY() - vector.startPoint.getY();
    Point finalPoint = new Point(x + deltaX, y + deltaY);
    return finalPoint;
  }

  public Point plus(long deltaX, long deltaY) {
    Point finalPoint = new Point(x + deltaX, y + deltaY);
    return finalPoint;
  }

  public String toString() {
    return (x + "," + y);
  }

}
