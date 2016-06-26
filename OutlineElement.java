public class OutlineElement {

  protected double theta = 0; // amount of turn
  protected double length = 0;
  protected double startDirection = 0; // radians
  protected double endDirection = 0; // radians
  protected Point startPoint = new Point(0,0);
  protected Point endPoint = new Point(0,0);
  protected Point controlPointOne = null;
  protected Point startPointTwo = null;

  protected double maxX = 0;
  protected double maxY = 0;
  protected double minX = 0;
  protected double minY = 0;

  protected boolean likelyDuplicate = false;
  protected boolean offsetGenerated = false;
  protected boolean aQuadraticBezier = false;
  protected boolean isALine = false;
  protected boolean isBezierSegment = false;
  protected boolean redundantLine = false;
  protected int strokeThickness = 0;

  public OutlineElement nextElement = null;
  public OutlineElement previousElement = null;

  public static int bezierNumber = 0;

  public Point focus = new Point(0,0);
  public double radiusOfCurvature = 0;
  protected int bezierSteps = 181;
  // 180 divides nicely by a lot of whole numbers..
  protected Point [] bezierPoints = new Point [bezierSteps];

  public OutlineElement() {
  }

  public OutlineElement copyOf() {
    OutlineElement ret = new OutlineElement();
    return ret;
  }

  public Point start() {
    return startPoint;
  }

  public Point end() {
    return endPoint;
  }

  public Point intersectsAt() {
    return new Point(0,0);
  }

  public boolean isLine() {
    return isALine;
  }

  public boolean isQuadratic() {
    return aQuadraticBezier;
  }

  public boolean isBezierSegment() {
    return isBezierSegment;
  }

  public boolean isLikelyDuplicate() {
    return likelyDuplicate;
  }

  public boolean isOppositeLimbEdge (Line otherLine, int thickness) {
    return false;
  }

  public boolean likelyFillet(int thickness) {
    return false;
  }

  public boolean likelyFillet(int thickness, int pathDir) {
    return false;
  }

  public boolean likelyEndCap() {
    return false;
  }

  public boolean likelyEndCap(int thickness) {
    return false;
  }

  public boolean likelyEndCap(int thickness, int pathDir) {
    return false;
  }

  public boolean likelySerif(int thickness) {
    return false;
  }

  public boolean smallVertical(int thickness) {
    return false;
  }

  public void setAsLikelyDuplicate() {
    likelyDuplicate = true;
  }

  public void setAsBezierSegment() {
    isBezierSegment = true;
  }

  public void makeRedundant() {
    redundantLine = true;
  }

  public boolean isRedundant() {
    return redundantLine;
  }

  public void newEnd(Point newEndPoint) {
    endPoint = newEndPoint;
  }

  public void newStart(Point newStartPoint) {
    startPoint = newStartPoint;
  }

  public boolean isNearlyCollinear(Line other, int thickness) {
    return false;
  }

  public double deltaTheta() {
    return theta;
  }

  public static double length(Point P1, Point P2) {
    long deltaY = P1.getY() - P2.getY();
    long deltaX = P1.getX() - P2.getX();
    return Math.sqrt(deltaX*deltaX + deltaY*deltaY);
  }

  public double length() {
    return length;
  }

  public double crudeLength() {
    return length;
  }

  public Line spanningLine() {
    return new Line(startPoint, endPoint);
  }

  public double startDirection() {
    return startDirection;
  }

  public double endDirection() {
    return endDirection;
  }

  public void setStrokeThickness(int thickness) {
    strokeThickness = thickness;
  }

  public boolean possibleVertex() {
    return false; // default scenario
  }

  public double maximumX() {
    if (startPoint.getX() > maxX ) {
      maxX = startPoint.getX();
    } else if (endPoint.getX() > maxX ) {
      maxX = endPoint.getX();
    } else if (controlPointOne != null) { // bezier calcs here 
      walkBezierCurve(); // to sort out curve's minX, maxX, minY, MaxY
    }
    return maxX;
  }

  public double maximumY() {
    if (startPoint.getY() > maxY ) {
      maxY = startPoint.getY();
    } else if (endPoint.getY() > maxY ) {
      maxY = endPoint.getY();
    } else if (controlPointOne != null) { // bezier calcs here 
      walkBezierCurve(); // to sort out curve's minX, maxX, minY, MaxY
    }
    return maxY;
  }

  public double minimumX() {
    if (startPoint.getX() < minX ) {
      minX = startPoint.getX();
    } else if (endPoint.getX() < minX ) {
      minX = endPoint.getX();
    } else if (controlPointOne != null) { // bezier calcs here 
      walkBezierCurve(); // to sort out curve's minX, maxX, minY, MaxY
    }
    return minX;
  }

  public double minimumY() {
    if (startPoint.getY() < minY ) {
      minY = startPoint.getY();
    } else if (endPoint.getY() < minY ) {
      minY = endPoint.getY();
    } else if (controlPointOne != null) { // bezier calcs here 
      walkBezierCurve(); // to sort out curve's minX, maxX, minY, MaxY
    }
    return minY;
  }

  public String toOctaveLine() {
    if (offsetGenerated || redundantLine) {
      return "";
    } else if (isALine) {
      return octaveLine(startPoint, endPoint);
    } else if (aQuadraticBezier) {
      //  System.out.println("%We have a bezier, time to make lines");
      String segments = "";
      int stepDivisor = 6;
      int stepSize = (bezierPoints.length-1)/stepDivisor; 
      for (int index = 0;
           index < (bezierPoints.length - stepSize);
           index += stepSize) {
        segments = segments
            + octaveLine(bezierPoints[index],
                         bezierPoints[index + stepSize]);
        //  System.out.println("%Index: " + index);
      }
      return segments;
    } else {
      return ""; // shouldn't get to here, hopefully
    }
  }

  public String toElementLine(int thickness) {
    return toElementLine(thickness, 1);
  }

  public String toElementLine(int thickness, double magnification) {
    if (isALine) {
      return elementLine(startPoint,
                         endPoint,
                         thickness,
                         magnification);
    } else if (aQuadraticBezier) {
      //  System.out.println("%We have a bezier, time to make lines");
      String segments = "";
      bezierNumber++;
      int stepDivisor = 6;
      int stepSize = (bezierPoints.length-1)/stepDivisor; 
      for (int index = 0;
           index < (bezierPoints.length - stepSize);
           index += stepSize) {
        segments = segments
            + labelledElementLine(bezierPoints[index],
                                  bezierPoints[index + stepSize],
                                  thickness,
                                  magnification,
                                  bezierNumber);
        //  System.out.println("%Index: " + index);
      }
      return segments + "#\n"; // make it easier to see what's what
    } else {
      return "";
    }
  }

  public static String labelledElementLine(Point start,
                                           Point end,
                                           int thickness,
                                           double magnification,
                                           int label) {
    return "ElementLine["
        + (long)(start.getX()*magnification)
        + " "
        + (long)(-start.getY()*magnification)
        + " "
        + (long)(end.getX()*magnification)
        + " "
        + (long)(-end.getY()*magnification)
        + " "
        + (long)(thickness*magnification)
        + "]#bezier: " + label  // not being called currently
        + "\n";
  }

  public static String elementLine(Point start,
                                   Point end,
                                   int thickness,
                                   double magnification) {
    return "ElementLine["
        + (long)(start.getX()*magnification)
        + " "
        + (long)(-start.getY()*magnification)
        + " "
        + (long)(end.getX()*magnification)
        + " "
        + (long)(-end.getY()*magnification)
        + " "
        + (long)(thickness*magnification)
        + "]\n";
  }

  public static String octaveLine(Point start, Point end) {
    return "line(["
        + start.getX()
        + " " 
        + end.getX()
        + "],["
        + start.getY()
        + " "
        + end.getY()
        + "])\n";
  }


  public void walkBezierCurve() {
    // quadratic for now
    // ignore second control point
    // which is ok for truetype fonts
    double t = 0;
    double deltaX1 = controlPointOne.getX() - startPoint.getX();
    double deltaX2 = endPoint.getX() - controlPointOne.getX();
    double deltaY1 = controlPointOne.getY() - startPoint.getY();
    double deltaY2 = endPoint.getY() - controlPointOne.getY();
    //System.out.println("% Bezier: deltaX1: " + deltaX1
    //                   + ", deltaY1: " + deltaY1
    //                   + ", deltaX2: " + deltaX2
    //                   + ", deltaY2: " + deltaY2);
    double tempX1 = startPoint.getX();
    double tempY1 = startPoint.getY();
    double tempX2 = controlPointOne.getX();
    double tempY2 = controlPointOne.getY();
    double currentX = tempX1;
    double currentY = tempY1;
    double currentDeltaX = 0.0;
    double currentDeltaY = 0.0;
    Point firstNormalOrigin = new Point(0,0);
    Point secondNormalOrigin = new Point(0,0);
    Line firstNormal = new Line(0,0,0,0);
    Line secondNormal = new Line(0,0,0,0);
    Line tangent = new Line(0,0,0,0);
    double normalOneRadius = 0;
    double normalTwoRadius = 0;
    minX = tempX1;
    minY = tempY1;
    maxX = tempX1;
    maxY = tempY1;

    // this fixes discontinuities at bezier start and finish
    bezierPoints[0] = startPoint;
    bezierPoints[bezierSteps-1] = endPoint;
    for (int steps = 1; steps < (bezierSteps - 1); steps++) {
    //
      tempX1 = startPoint.getX() // tempX1
          + ((1.0*steps)/bezierSteps)*deltaX1;
      tempY1 = startPoint.getY() // tempY1
          + ((1.0*steps)/bezierSteps)*deltaY1;
      tempX2 = controlPointOne.getX() // tempX2
          + ((1.0*steps)/bezierSteps)*deltaX2;
      tempY2 = controlPointOne.getY() // tempY2
          + ((1.0*steps)/bezierSteps)*deltaY2;
      // System.out.println("% Bezier: tempX1: " + tempX1
      //                   + ", tempY1: " + tempY1
      //                   + ", tempX2: " + tempX2
      //                   + ", tempY2: " + tempY2);
      currentDeltaX = tempX2 - tempX1;
      currentDeltaY = tempY2 - tempY1;
      currentX = tempX1 + ((1.0*steps)/bezierSteps)*currentDeltaX;
      currentY = tempY1 + ((1.0*steps)/bezierSteps)*currentDeltaY;
      //      System.out.println("% Bezier: CurrentX: " + currentX
      //                   + ", CurrentY: " + currentY);
      bezierPoints[steps] = new Point(currentX, currentY);
      if (steps == (int)(1.0*(bezierSteps-1))/3.0) {
        firstNormalOrigin = bezierPoints[steps];
        tangent = new Line(currentX, currentY, tempX2, tempY2);
        firstNormal = tangent.CWNormalTo();
      }
      if (steps == (int)(2.0*(bezierSteps-1)/3.0)-1) {
        secondNormalOrigin = bezierPoints[steps];
        tangent = new Line(currentX, currentY, tempX2, tempY2);
        secondNormal = tangent.CWNormalTo();
      }
      if (tempX1 > maxX) {
        maxX = tempX1;
      }
      if (tempX1 < minX) {
        minX = tempX1;
      }
      if (tempY1 > maxY) {
        maxY = tempY1;
      }
      if (tempY1 < minY) {
        minY = tempY1;
      }
      // System.out.println("% Max x,y: " + maxX + ", " + maxY +
      //                  "Min x,y" + minX + ", " + minY);
    }
    // this assumes the curve will have a sensible focus
    // and radius of curvature, and that the normals
    // are not close to parallel, in which case results
    // might be rubbish
    focus = firstNormal.intersectsAt(secondNormal);
    // we now average the lengths of the two normals
    // to estimate the radius of curvature
    radiusOfCurvature = (Line.length(firstNormalOrigin, focus) +
                         Line.length(secondNormalOrigin, focus))/2.0;
    //    System.out.println("%Focus: " + focus.getX()
    //                     + ", " + focus.getY());
    //System.out.println("%Radius of Curvature: "
    //                   + radiusOfCurvature);
  }  

  public Line generateCentrelineCW() {
    return new Line(0,0,0,0);
  }

  public Line generateCentrelineCW(int limbThickness) {
    return new Line(0,0,0,0);
  }

  public Line [] generateCentrelineCWArray() {
    Line [] retArray = new Line[1];
    retArray[0] = new Line(0,0,0,0);
    return retArray;
  }

  public Line [] generateCentrelineCWArray(int limbThickness) {
    Line [] retArray = new Line[1];
    retArray[0] = new Line(0,0,0,0);
    return retArray;
  }

  public Line[] generateCentrelineArray(int Thickness, double plusTheta) {
    Line [] retArray = new Line[1];
    retArray[0] = new Line(0,0,0,0);
    return retArray;
  }

  public Line [] toLineArray() {
    Line [] segments = new Line[1];
    segments[0] = new Line(0,0,0,0);
    return segments;
  }

  public static String
    outlineElementArrayToOctave(OutlineElement[] elementArray) {
    String returnedOctaveLines = "";
    for (int index = 0; index < elementArray.length; index++) {
      returnedOctaveLines
          = returnedOctaveLines
          + elementArray[index].toOctaveLine();
    }
    return returnedOctaveLines;
  }

  public static OutlineElement[] elementArrayCombine(OutlineElement[] first, OutlineElement[] second) {
    OutlineElement [] newArray
        = new OutlineElement[first.length + second.length];
    for (int index = 0; index < first.length; index ++) {
      newArray[index] = first[index];
    }
    for (int index = 0; index < second.length; index ++) {
      newArray[index + first.length] = second[index];
    }
    return newArray;
  }

  public static void flagDuplicateElements(OutlineElement[] theArray,
                                           int size,
                                           int testThickness) {
    boolean hasDuplicate = false;
    for (int index = 0; index < (size - 1); index ++) {
      //      hasDuplicate
      //    |= theArray[size-1].isOppositeLimbEdge(theArray[index],testThickness);
    }
    if (hasDuplicate) {
      theArray[size-1].makeRedundant(); // simple concept test
    }
    if (size > 1) {
      flagDuplicateElements(theArray,
                            (size - 1),
                            testThickness);
    }
  }

  public static Line scaledVector(Line vector, double scalar) {
    Point beginning = new Point(vector.startPoint);
    Line dirVector = asVector(vector);
    long newEndX = beginning.getX() + 
        (long)(dirVector.endPoint.getX()*scalar);
    long newEndY = beginning.getY() +
        (long)(dirVector.endPoint.getY()*scalar);
    Point newEnd = new Point(newEndX,newEndY);
    return new Line(beginning, newEnd);
  }

  public static Line asVector(Line a) {
    long deltaX = a.endPoint.getX() - a.startPoint.getX();
    long deltaY = a.endPoint.getY() - a.startPoint.getY();
    return new Line(0, 0, deltaX, deltaY);
  }

  public static double vectorLength(Line a) {
    Line vectorA = asVector(a);
    double calculatedLength = Math.sqrt(dotProduct(vectorA, vectorA));
    return calculatedLength;
  }

  public static double dotProduct(Line a, Line b) {
    Line vecA = asVector(a);
    Line vecB = asVector(b);
    return (vecA.endPoint.getX()*vecB.endPoint.getX()
            + vecA.endPoint.getY()*vecB.endPoint.getY()); 
  }

  public static double cosTheta(Line a, Line b) {
    double dotProd = dotProduct(a, b); // using: a.b = |a||b|cos(theta)
    //System.out.println("__Latest dot product: " + dotProd
    //                   + "  for lines: " + a + " ,  and   " + b);
    double lengthProduct = vectorLength(a)*vectorLength(b);
    if (lengthProduct == 0) {
      return 1.0;  //degenerate case, no angle between line and point
    } else {
      return (dotProd/lengthProduct);
    }
  }

  public static boolean isHorizontal(Line a) {
    Line horizontal = new Line(0,0,100,0);
    double cos = cosTheta(a, horizontal);
    if ((cos < -0.999) || (cos > 0.999)) {
      return true;
    }
    return false;
  }

  public static boolean isVertical(Line a) {
    Line horizontal = new Line(0,0,100,0);
    double cos = cosTheta(a, horizontal);
    if ((cos < 0.001) && (cos > -0.001)) {
      return true;
    }
    return false;
  }

  public static boolean inYPlus(Line a) {
    if (direction(a) <= Math.PI) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean inYMinus(Line a) {
    if (direction(a) > Math.PI) {
      return true;
    } else {
      return false;
    }
  }


  public static double direction(Point first, Point last) {
    Line unitVector = new Line(0,0,1,0);
    Line b = new Line(first, last);
    Double calculatedTheta = Math.acos(cosTheta(b, unitVector));
        long deltaY = last.getY() - first.getY();
    double finalDir = calculatedTheta;
    if (deltaY < 0) {
      finalDir = Math.PI*2-calculatedTheta;
      // could do (0 - calculatedTheta), hmmm
    }
    return finalDir;
  } 

  public static double direction(Line theLine) {
    return direction(theLine.startPoint, theLine.endPoint);
  }

}
