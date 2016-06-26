import java.lang.Math;

public class Line extends OutlineElement {

  // some constructors
  public Line(Point first, Point second) {
    startPoint = new Point(first);
    endPoint = new Point(second);
    aQuadraticBezier = false;
    isALine = true;
  }

  public Line(Line other) {
    startPoint = new Point(other.startPoint);
    endPoint = new Point (other.endPoint);
    aQuadraticBezier = false;
    isALine = true;
    if (other.isBezierSegment()) {
      isBezierSegment = true;
    }
  }


  public Line(long X1, long Y1, long X2, long Y2) {
    startPoint = new Point(X1, Y1);
    endPoint = new Point(X2, Y2);
    aQuadraticBezier = false;
    isALine = true;
  }

  public Line(double X1, double Y1, double X2, double Y2) {
    startPoint = new Point((long)X1, (long)Y1);
    endPoint = new Point((long)X2, (long)Y2);
    aQuadraticBezier = false;
    isALine = true;
  }

  public Line(long X, long Y, double radius, double theta) {
    double deltaX = radius*Math.cos(theta);
    double deltaY = radius*Math.sin(theta);
    startPoint = new Point(X, Y);
    endPoint = new Point(deltaX, deltaY);
    aQuadraticBezier = false;
    isALine = true;
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

  public Line copyOf() {
    Line retLine = new Line(this);
    if (this.isBezierSegment() ) {
      retLine.setAsBezierSegment();
    }
    return retLine;
  }

  public Line CWNormalTo() {
    // treats line as tangent, starting at startPoint
    // and needing a normal returned at -Pi/2 to the
    // direction, i.e. a normal
    Point startingPoint = new Point(startPoint);
    Line directionVector = asVector(this);
    long deltaX = directionVector.endPoint.getX();
    long deltaY = directionVector.endPoint.getY();
    // a rotation matrix simplifies to this for -PI/2 rotation:
    Point newEndPoint = startingPoint.plus(deltaY, -deltaX);
    return new Line(startingPoint, newEndPoint);

    /*
      double normalDir = startDirection()-Math.PI/2;
      long startX = startPoint.getX();
      long startY = startPoint.getY();
      Point endingPoint = new Point(startX+400*Math.cos(-normalDir), 
      startY+400*Math.sin(-normalDir));
      // 400 is a nice round number. Hopefully big enough to avoid
      // too much error when calculating parallelism, distances, etc... 
      return new Line(startingPoint, endingPoint);
    */
  }

  public double endDirection() {
    return direction(startPoint, endPoint);
  }

  public double startDirection() {
    return endDirection(); // same when it's a line
  } 

  public double deltaTheta() {
    theta = 0; // for a line
    return theta;
  }

  public boolean likelySerif(int thickness) {
    if (isHorizontal(this) && (length() < thickness)) {
      return true;
    } else {
      return false;
    }
  }

  public boolean smallVertical(int thickness) {
    if (isVertical(this) && (length() < thickness)) {
      return true;
    } else {
      return false;
    }
  }

  public double length() {
    return length(startPoint, endPoint);
  }

  public double crudeLength() {
    return length();
  }

  public boolean isParallelTo(Line otherLine) {
    Line a = new Line(startPoint, endPoint);
    Double calculatedTheta = Math.acos(cosTheta(a, otherLine));
    if (calculatedTheta < 0.2) { 
      return true;
    } else {
      return false;
    } 
  }

  public boolean isAntiParallelTo(Line otherLine) {
    Line a = new Line(startPoint, endPoint);
    Double calculatedTheta = Math.acos(cosTheta(a, otherLine));
    if (calculatedTheta > (Math.PI - 0.2)) { 
      return true;
    } else {
      return false;
    } 
  }

  public boolean isOppositeLimbEdge (Line otherLine, int thickness) {
    if (isParallelTo(otherLine) || !isAntiParallelTo(otherLine)) {
      return false;
    } // if parallel, it can't be the other side of the same limb... 
    // we get to here if the lines are vaguely antiparallel
    // now we check if they are ~= "thickness" apart
    System.out.println("% Distance for oppositeLimbEdge test" + distanceFrom(otherLine));
    if ((distanceFrom(otherLine) - thickness) < thickness/5.0) {
      return true; // we are closer than .20 thickness
    } else {
      return false;
    }
  }


  public boolean isNearlyCollinear(Line otherLine, int thickness) {
    if (!isParallelTo(otherLine) && !isAntiParallelTo(otherLine)) {
      return false;
    }
    // we get to here if the lines are vaguely parallel/antiparallel
    // now we check if they are pretty darned close
    if (distanceFrom(otherLine) < thickness/6.0) {
      return true; // we are closer than .16 thickness
    } else {
      return false;
    }
  }

  public boolean isCollinear(Line otherLine) {
    if (!isParallelTo(otherLine) && !isAntiParallelTo(otherLine)) {
      return false;
    }
    // assumes start points not the same
    Line testLine = new Line(startPoint, otherLine.startPoint);
    return (isParallelTo(otherLine) || isAntiParallelTo(otherLine));
  }

  public Point intersectsAt(Line otherLine) {
    return intersectsAt(otherLine, false);
  }

  public Point intersectsAt(Line otherLine, boolean parallelCheck) {
    long deltaY1 = endPoint.getY() - startPoint.getY();
    long deltaX1 = endPoint.getX() - startPoint.getX();
    long deltaY2 = otherLine.endPoint.getY()
        - otherLine.startPoint.getY();
    long deltaX2 = otherLine.endPoint.getX()
        - otherLine.startPoint.getX();
    boolean verbose = false;
    if (verbose) {
      System.out.println("%DeltaX1: " + deltaX1);
      System.out.println("%DeltaY1: " + deltaX2);
      System.out.println("%DeltaX2: " + deltaY1);
      System.out.println("%DeltaY2: " + deltaY2);
    }
    double A1 = 0.0; // y1 = A1*x + B1
    double A2 = 0.0; // y2 = A2*x + B2
    double B1 = 0.0;
    double B2 = 0.0;
    if (verbose) {
      System.out.println("%A1: " + A1);
      System.out.println("%A2: " + A2);
      System.out.println("%B1: " + B1);
      System.out.println("%B2: " + B2);
    }
    double intersectionX = 0.0;
    double intersectionY = 0.0;

    if ((!isParallelTo(otherLine)
         && !isAntiParallelTo(otherLine))
        || !parallelCheck) {
      if (deltaX1 != 0) {
        A1 = (deltaY1*1.0)/deltaX1; // convert to double
        B1 = endPoint.getY() - A1*endPoint.getX();
      }
      if (deltaX2 != 0) {
        A2 = (deltaY2*1.0)/deltaX2; // convert to double
        B2 = otherLine.endPoint.getY() - A2*otherLine.endPoint.getX();
      }
      if (deltaX1 == 0) { // i.e. first line vertical
        intersectionX = endPoint.getX();
      } else if (deltaX2 == 0) { // i.e. second line vertical
        intersectionX = otherLine.endPoint.getX();
      } else { //  neither line vertical
        intersectionX = (1.0*(B1-B2))/(-A1+A2);  // convert to double
      }
      if (verbose) {
        System.out.println("%A1: " + A1);
        System.out.println("%A2: " + A2);
        System.out.println("%B1: " + B1);
        System.out.println("%B2: " + B2);
      }
      if (deltaX1 != 0) { // i.e. first line not vertical
        intersectionY = A1*intersectionX + B1;
      } else { // i.e. second line not vertical
        intersectionY = A2*intersectionX + B2;
      }
    }
    return new Point((long)intersectionX, (long)intersectionY);
  }

  public double distanceFrom(Line otherLine) {
    Line normal = CWNormalTo();
    Point intersection = normal.intersectsAt(otherLine);
    return length(startPoint, intersection);
  }

  public Point midPoint() {
    long midX = startPoint.getX()
        + (endPoint.getX()-startPoint.getX())/2;
    long midY = startPoint.getY()
        + (endPoint.getY()-startPoint.getY())/2;
    return new Point(midX, midY);
  }

  public Line[] generateCentrelineCWArray() {
    Line[] retArray = new Line[1];
    Line tempLine
        = generateCentrelineCW(strokeThickness); //use default
    retArray[0] = tempLine;
    return retArray;
  }

  public Line[] generateCentrelineCWArray(int lineThickness) {
    Line[] retArray = new Line[1];
    Line tempLine = generateCentrelineCW(lineThickness); //use default
    retArray[0] = tempLine;
    return retArray;
  }

  public Line generateCentrelineCW() {
    return generateCentrelineCW(strokeThickness); //use default
  }

  public Line generateCentrelineCW(int lineThickness) {
    return generateCentreline(lineThickness, (-Math.PI/2.0));
  }

  public Line generateCentrelineCCW() {
    return generateCentrelineCCW(strokeThickness); //use default
  }

  public Line generateCentrelineCCW(int lineThickness) {
    return generateCentreline(lineThickness, Math.PI/2.0);
  }

  public Line generateCentreline(int Thickness, double plusTheta) {
    strokeThickness = Thickness;
    long newStartX = startPoint.getX();
    long newStartY = startPoint.getY();
    long newEndX = endPoint.getX();
    long newEndY = endPoint.getY();

    Line normal = asVector(CWNormalTo());
    double normalLength = vectorLength(normal);
    Line scaledVec = scaledVector(normal,
                                  strokeThickness/(2.0*normalLength));

    //    System.out.println("%EndDirection: "
    //                   + endDirection()/(2*Math.PI)*360);
    newStartX += Math.sin(endDirection())*strokeThickness/2.0;
    newStartY -= Math.cos(endDirection())*strokeThickness/2.0;
    newEndX += Math.sin(endDirection())*strokeThickness/2.0;
    newEndY -= Math.cos(endDirection())*strokeThickness/2.0;

    //    Point newStart = startPoint.plus(scaledVec);
    //    Point newEnd = endPoint.plus(scaledVec);

    //    Line returnLine = new Line(newStart, newEnd);
    Line returnLine = new Line(newStartX, newStartY, newEndX, newEndY);
    if (this.isBezierSegment()) {
      returnLine.setAsBezierSegment();
    }
    //System.out.println("##converting Line to offsetLine...");
    //if (returnLine.isBezierSegment()) {
    //  System.out.println("##...  and line is a bezierSegment()...");
    //}
    return returnLine;
  }

  public Line extendToNearLine(Line targetLine, int thickness, double inclusionThreshold) {
    // inclusion threshold is a double valued proportion of the
    // line thickness, within which an endpoint will be joined to a
    // "nearby" line that it intersects
    // assumes lines are not parallel at this stage.
    // Lines to be stitched could be almost 1.0*thickness apart
    // although T junctions would be ~0.5*thickness apart
    // and threshold needs to take this into consideration
    // ... could perhaps do calculations based on deltaDirection
    // to determine required thresold
    Point targetPoint = this.intersectsAt(targetLine);
    double nearby = inclusionThreshold*thickness; 
    if (length(targetPoint, endPoint) < nearby) {
      endPoint = targetPoint;
    }
    if ((length(targetPoint, startPoint) < nearby) &&
      (length(targetPoint, startPoint) < 
       length(targetPoint, endPoint))) {
      startPoint = targetPoint;
    } // this picks the end that is closest, and within
    // the required joining threshold/zone
    return new Line(startPoint, endPoint);
  }

  public Line extendHeightTo(long targetY, int thickness) {
    // This is used to replicate the effect of terminal beziers
    // with the rounded end of the line finishing where the
    // bezier outline closes off the limb of the glyph.
    // The method looks for the end of the line which has
    // the closest y value, and if it is within the provided
    // thickness value, will stretch the line a little to make
    // it finish up at the right height
    // The simplest way is to do +/- inclusion bounds for 
    // the target Y value, and then check the start and end
    // points, and choose the one which is closest and falls
    // within the inclusion bounds
    // Ideally, check if terminal before running this method
    // Furthermore, should supply method with a targetY that
    // already takes into account the offset required for
    // thickness/2, since an isolated line will struggle to
    // know which end is of interest, and some, like the
    // letter "l" will have two terminal ends.
    // ***Assumes line is not parallel to x axis
    double threshold = 0.25;
    // We'll stretch or shorten if point is within "threshold"
    // of the target Y height
    Line targetYLine = new Line(0,targetY,100,targetY);
    return extendToNearLine(targetYLine, thickness, threshold);
  }

  public Line extendToIncorporate(Line collinearLine, long maxGap) {
    if (!this.isCollinear(collinearLine)) {
      return this; // what, you call that collinear!?!?!
    } else if (collinearGap(this, collinearLine)
               > maxGap) {
      return this; // gap is too big, i.e., think omega
    } else {
      // now, we are going on a walk along an outline.
      // accordingly, a collinear line is usually in
      // front of the current line, except, for, say, H.
      // Simples, eh?
      // um, no, actually
      // simpler way to do it may be to check if direction
      // of current.endpoint -> otherStartpoint is same
      // as direction of currentLine, and proceed 
      // accordingly 
      Point currentStart = this.startPoint;
      Point currentEnd = this.endPoint;
      Line returnLine = new Line(currentStart, currentEnd);
      if (length(currentStart, collinearLine.startPoint) > length()) {
        returnLine =
            new Line(returnLine.startPoint, collinearLine.startPoint);
      }
      if (length(returnLine.startPoint, collinearLine.endPoint)
          > returnLine.length()) {
        returnLine =
            new Line(returnLine.startPoint, collinearLine.endPoint);
      }
      if (length(collinearLine.startPoint, currentEnd)
          > returnLine.length()) {
        returnLine = new Line(collinearLine.startPoint, currentEnd);
      }
      return returnLine;
    }
  }

  public double collinearGap(Line collinearLine) {
    return collinearGap(this, collinearLine);
  }

  public static double collinearGap (Line firstLine, Line secondLine) {
    double shortestLength = 1000000; // pick something unlikely
    if (length(secondLine.endPoint, firstLine.startPoint)
        < shortestLength) {
      shortestLength
          = length(secondLine.endPoint, firstLine.startPoint);
    }
    if (length(secondLine.startPoint, firstLine.startPoint)
        < shortestLength) {
      shortestLength
          = length(secondLine.startPoint, firstLine.startPoint);
    }
    if (length(secondLine.endPoint, firstLine.endPoint)
        < shortestLength) {
      shortestLength
          = length(secondLine.endPoint, firstLine.endPoint);
    }
    if (length(secondLine.startPoint, firstLine.endPoint)
        < shortestLength) {
      shortestLength
          = length(secondLine.startPoint, firstLine.endPoint);
    }
    return shortestLength;
  }

  public Line [] toLineArray() {
    Line [] lineSegments = new Line [1];
    lineSegments[0] = new Line(this);
    return lineSegments;
  }

  public String toString() {
    return (startPoint + "->" + endPoint);
  }

}
