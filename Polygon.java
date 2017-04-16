import java.lang.Math;
import java.util.ArrayList;

public class Polygon extends OutlineElement {

  ArrayList<Point> pointList = new ArrayList<Point>();

  private long maximumWidth = 0;
  private long xMin = 10000000;
  private long xMax = -10000000; 

  // some constructors
  public Polygon(Line[] lines) {
    for (int i = 0; i < lines.length; i++) {
	Point vertex = new Point(lines[i].startPoint);
	pointList.add(vertex);
	endPoint = vertex;
    }
    if (pointList.size() >= 1) {
	startPoint = pointList.get(0);
    }
    aQuadraticBezier = false;
    isALine = true;
  }

  public Polygon(Polygon other) {
    for (int i = 0; i < other.pointList.size(); i++) {
        Point vertex = new Point(other.pointList.get(i));
        pointList.add(vertex);
        endPoint = vertex;
    }
    if (pointList.size() >= 1) {
        startPoint = pointList.get(0);
    }
    aQuadraticBezier = false;
    isALine = true;
  }

  public Polygon(Point[] pointListOrig) {
    for (int i = 0; i < pointListOrig.length; i++) {
        Point vertex = new Point(pointListOrig[i]);
        pointList.add(vertex);
        endPoint = vertex;
    }
    if (pointList.size() >= 1) {
        startPoint = pointList.get(0);
    }
    aQuadraticBezier = false;
    isALine = true;
  }

  private long findExtents(double magnification) {
    int nVertices = pointList.size();
    for (int i = 0; i < (nVertices); i++) {
      long xCoord = (long)(pointList.get(i).getX()*magnification);
      if (xCoord < xMin) {
        xMin = xCoord;
      }
      if (xCoord > xMax) {
        xMax = xCoord;
      }
      maximumWidth = (xMax - xMin)/100; // in mil
    }
    return maximumWidth;
  } 


  public long maximumWidth(double magnification) { // used for polygons in .lht
    return findExtents(magnification);
  }

  public long polyMinimumX(double magnification) { // used for polygons in .lht
    findExtents(magnification);
    return xMin;
  }

  public long polyMaximumX(double magnification) { // used for polygons in .lht
    findExtents(magnification);
    return xMax;
  }

  public void addPoint(Point p) {
    Point vertex = new Point(p);
    pointList.add(vertex);
    endPoint = vertex;
  }

  public void addPoint(long X, long Y) {
    Point vertex = new Point(X, Y);
    pointList.add(vertex);
    endPoint = vertex;
  }

  public void addPoint(double X, double Y) {
    Point vertex = new Point((long)X, (long)Y);
    pointList.add(vertex);
    endPoint = vertex;
  }

  public Point end() {
    return endPoint;
  }

  public Point start() {
    return startPoint;
  }


  public void newEnd(Point newEndPoint) {
    endPoint = newEndPoint;
  }

  public void newStart(Point newStartPoint) {
    startPoint = newStartPoint;
  }

  public boolean isBezierSegment() {
    return isBezierSegment;
  }

  public Polygon copyOf() {
    Polygon retPolygon = new Polygon(this);
    return retPolygon;
  }

  public Point centroid() {
    int nVertices = pointList.size();
    long aveY = 0;
    long aveX = 0;
    for (int i = 0; i < (nVertices - 1); i++) {
        aveX += pointList.get(i).getX();
	aveY += pointList.get(i).getY();
    }
    aveX = aveX/nVertices;
    aveY = aveY/nVertices;
    Point centroid = new Point(aveX, aveY);
    return centroid;
  }

  public Line [] toLineArray() {
    int nVertices = pointList.size();
    Line [] lineSegments = new Line [nVertices];
    for (int i = 0; i < (nVertices - 1); i++) {
    	lineSegments[i] = new Line(pointList.get(i), pointList.get(i+1));
    }
    if (nVertices != 0) {
    	lineSegments[nVertices-1] = new Line(pointList.get(nVertices-1), pointList.get(0));
    }
    return lineSegments;
  }

  public String toString() {
    return ("Polygon starting at" + startPoint + "->" + endPoint);
  }

  public String toGEDAPolygon() {
    return toGEDAPolygon(1.0, 0, 0, true);
  }

  public String toGEDAPolygon(double magnification, long yOffset, long xOffset, boolean legacy) {
    System.out.println("converting Polygon object to gEDA polygon definition.");
    int nVertices = pointList.size();
    if (legacy) {
      String retPolygon = "\tPolygon(\"clearpoly\")\n\t(\n\t\t";
      for (int i = 0; i < (nVertices); i++) {
  	    retPolygon = retPolygon
               + "[" + (long)(pointList.get(i).getX()*magnification) + " "
  	       +  (long)(yOffset-pointList.get(i).getY()*magnification) + "]";
  	if (i != (nVertices - 1) && ((i+1)%5 != 0)) {
  	    retPolygon = retPolygon + " ";
          } else if (i != (nVertices - 1) && ((i+1)%5 == 0)) {
              retPolygon = retPolygon + "\n\t\t";
  	} else {
  	    retPolygon = retPolygon + "\n\t)\n";
          }
      }
      return retPolygon;
    } else {
      String retPolygon = "";
	// reduce resolution of extremely close vertices prone to self intersection
	long threshold = 0;
	long lastX = 0;
	long lastY = 0;
      for (int i = 0; i < (nVertices); i++) {

        long xCoord
            = (long)(pointList.get(i).getX()*magnification);
        long yCoord = (long)(yOffset-pointList.get(i).getY()*magnification); 
	if ((i == 0) || (i > 0
              && ((xCoord - lastX)*(xCoord - lastX) + (yCoord - lastY)*(yCoord - lastY)) > threshold)) {
          retPolygon = retPolygon
              + "       "
              + (xCoord - xOffset)/100.0 + "mil; "
              + yCoord/100.0 + "mil\n";
	}
	lastY = yCoord;
	lastX = xCoord;
      }
      return retPolygon;
    }
  }

}
