import java.lang.Math;

public class Quadratic extends OutlineElement {

  public Quadratic(long startX,
                   long startY,
                   long controlX,
                   long controlY,
                   long endX,
                   long endY) {
    startPoint = new Point(startX, startY);
    controlPointOne = new Point(controlX, controlY);
    endPoint = new Point(endX, endY);
    aQuadraticBezier = true;
    isALine = false;
    walkBezierCurve(); // initialise things
  }

  public Quadratic(Point start, Point control, Point end) {
    startPoint = start;
    controlPointOne = control;
    endPoint = end;
    aQuadraticBezier = true;
    isALine = false;
    walkBezierCurve(); // initialise things
  }

  public Quadratic(Quadratic other) {
    startPoint = new Point(other.startPoint);
    controlPointOne = new Point(other.controlPointOne);
    endPoint = new Point(other.endPoint);
    aQuadraticBezier = true;
    isALine = false;
    walkBezierCurve(); // initialise things
  }

  public Quadratic copyOf() {
    Quadratic retQuadratic = new Quadratic(this);
    return retQuadratic;
  }

  public String toString() {
    return (startPoint + "->" + controlPointOne + "->" + endPoint);
  }

  public boolean isQuadratic() {
    return aQuadraticBezier;
  }

  public boolean isOppositeLimbEdge (Quadratic otherElement, int thickness) {
    return false;
  }

  public Point focus() {
    return focus;
  }

  public double approxRadius() {
    //System.out.println("%%%%% RoC: " + radiusOfCurvature);
    return radiusOfCurvature;
  }

  public double endDirection() {
    Line vector1 = new Line(startPoint, controlPointOne);
    Line vector2 = new Line(controlPointOne, endPoint);
    if ((vector1.length() == 0) && (vector2.length() == 0)) {
      //System.out.println("___Degenerate case in endDirection()");
      return 0;
    } else if (vector2.length() == 0) {
      //System.out.println("___Degenerate case in endDirection()");
      return direction(vector1); // degenerate quadratic, CP1 = EP
    } else {
      return direction(vector2); // usual case
    }
  } 

  public double startDirection() {
    Line vector1 = new Line(startPoint, controlPointOne);
    Line vector2 = new Line(controlPointOne, endPoint);
    if ((vector1.length() == 0) && (vector2.length() == 0)) {
      //System.out.println("___Degenerate case in startDirection()");
      return 0;
    } else if (vector1.length() == 0) {
      //System.out.println("___Degenerate case in startDirection()");
      return direction(vector2); // degenerate quadratic, CP1 = SP
    } else {
      return direction(vector1); // usual case
    }
  } 

  public boolean isDegenerate() {
    return (startPoint.sameAs(controlPointOne)
            || endPoint.sameAs(controlPointOne));
  }

  public double deltaTheta() {
    /*theta = endDirection() - startDirection();
    System.out.println("__Quadratic delta theta:" + theta);
    //return theta; */

    Line vector1 = new Line(startPoint, controlPointOne);
    Line vector2 = new Line(controlPointOne, endPoint);
    Double calculatedTheta = Math.acos(cosTheta(vector1, vector2));
    theta = Math.PI - calculatedTheta;
    //System.out.println("__New quadratic delta theta:" + theta);
    if (((direction(vector2) - direction(vector1)) > Math.PI)
        || ((direction(vector2) - direction(vector1)) < 0)) {
      //System.out.println("__returning :" + (-theta));
      return -theta;
    } else {
      //System.out.println("__returning :" + (theta));
      return theta;
    }
  }

  public double crudeLength() {
    return length(startPoint, endPoint);
  }

  public double length() {
    Line[] tempLines = toLineArray();
    double estimatedLength = 0;
    for (Line segment : tempLines) {
      estimatedLength += segment.length();
    }
    return estimatedLength;
  }

  public Line spanningLine() {
    Line newLine = new Line(startPoint, endPoint);
    newLine.setAsBezierSegment();
    return newLine;
  }

  public boolean likelyFillet(int thickness) {
    //System.out.println("Testing quadratic for fillet crudeLength(): " +
    //                  crudeLength() );

    if (crudeLength() < thickness/2.0) {
      return true;
    } else {
      return false;
    }
  }


  public boolean likelyFillet(int thickness, int pathDir) {
    // CW is -1, CCW is +1 pathDir
    //System.out.println("Testing quadratic for fillet crudeLength(): " +
    //                  crudeLength() );
    //System.out.println("Testing quadratic for fillet deltaTheta(): " +
    //                  deltaTheta() );
    //System.out.println("Testing quadratic for fillet pathDir: " +
    //                  pathDir );
    if //((deltaTheta()*pathDir < 0) && 
        (crudeLength() < thickness/2.0) {
      return true;
    } else {
      return false;
    }
  }

  public boolean likelyEndCap(int thickness) {
    int pathDirCW = -1;
    int pathDirCCW = 1;
    //assume neither clockwise nor anticlockwise
    return (likelyEndCap(thickness, pathDirCW)
            || (likelyEndCap(thickness, pathDirCCW)));
  }
  
  public boolean likelyEndCap(int thickness, int pathDir) {
    // CW is -1, CCW is +1 pathDir
    //System.out.println("__LECpathDir : " + pathDir);
    //System.out.println("__LECdeltaTheta*pathDir : "
    //                   + deltaTheta()*pathDir);
    //System.out.println("__LECspanningLine().length() : " +
    //                   spanningLine().length());
    //System.out.println("__LECthickness : " + thickness);
    //System.out.println("__LECapproxRadius() : " + approxRadius());
    if ((deltaTheta()*pathDir > 0)
        && (spanningLine().length() < thickness)) {
        /*   if ( ((maximumX() - minimumX()) < thickness)
        && ((maximumY() - minimumY()) < thickness )
         || (deltaTheta() < 0) */

        //&& (approxRadius() < thickness*1.5) ) {
      
      return true;
    } else {
      return false;
    }
  }

  public boolean likelyEndCap() {
    return likelyEndCap(strokeThickness);
  }

  public boolean likelyInnerRadiusTo(Quadratic otherCurve, int thickness) {
    if (((otherCurve.approxRadius() - this.approxRadius())*(otherCurve.approxRadius() - this.approxRadius()) < 1.2*thickness*thickness) &&
        (length(otherCurve.focus, this.focus) < 1.2*thickness)) {
          return true;
        } else {
          return false;
        } 
  }

  public boolean likelyOuterRadiusTo(Quadratic otherCurve, int thickness) {
    // since we are using squared distances, inner or outer are the same
    // could refactor this
    return likelyInnerRadiusTo(otherCurve, thickness);
  }


  public Point centreOfCurvature() {
    // need calcs here to determine curvature K and/or
    // radius of curvature equal to 1/K as function of "t"
    // can see if rough and ready approach works
    // from the Bezier walk routine
    return focus;
  }

  public String toOctaveLineIfNotEndCap() {
    if (!likelyEndCap()) {
      return toOctaveLine();
    } else {
      return "";
    }
  }

  public Line [] toLineArray() {
    Line [] lineSegments;
    if (offsetGenerated) {
      lineSegments = null;
      return lineSegments;
    } else {
      //  System.out.println("%We have a bezier, time to make lines");
      String segments = "";
      int stepDivisor = 6;
      lineSegments = new Line [stepDivisor];
      int stepSize = (bezierPoints.length-1)/stepDivisor; 
      int segmentIndex = 0;
      for (int index = 0;
           index < (bezierPoints.length - stepSize);
           index += stepSize) {
        lineSegments[segmentIndex]
            = new Line(bezierPoints[index],
                       bezierPoints[index + stepSize]);
        //  System.out.println("%Index: " + index);
        lineSegments[segmentIndex].setAsBezierSegment();
        //System.out.println("##converting bezier to lineSegments[]...");
        //if (lineSegments[segmentIndex].isBezierSegment()) {
        //  System.out.println("##...  and line is a bezier...");
        //}
        segmentIndex++;
      }
      return lineSegments;
    }
  }


  public Line[] generateCentrelineCWArray() {
    return generateCentrelineCWArray(strokeThickness); //use default
  }

  public Line[] generateCentrelineCWArray(int lineThickness) {
    return generateCentrelineArray(lineThickness, (-Math.PI/2.0));
  }

  public Line[] generateCentrelineCCWArray() {
    return generateCentrelineCCWArray(strokeThickness); //use default
  }

  public Line[] generateCentrelineCCWArray(int lineThickness) {
    return generateCentrelineArray(lineThickness, Math.PI/2.0);
  }

  public Line[] generateCentrelineArray(int Thickness, double plusTheta) {
    Line [] initialSegments = this.toLineArray();
    Line [] offsetLines = new Line[initialSegments.length];
    for (int index = 0; index < initialSegments.length; index++) {
      offsetLines[index]
          = initialSegments[index].generateCentreline(Thickness, plusTheta);
      //System.out.println("##converting bezier line to offsetLine...");
      //if (offsetLines[index].isBezierSegment()) {
      //  System.out.println("##...  and new line is bezierSegment()...");
      //}
    }
    for (int index = 1; index < (initialSegments.length - 1); index++) {
      Line previous = offsetLines[index-1];
      Line current = offsetLines[index];
      Line spanLine
          = new Line(previous.end(),
                     current.start());
      if (spanLine.length() >= 1) { // consecutive lines not joined
        Point intersection // consecutive bezier segments with gap
            = spanLine.midPoint();
        //Point intersection = previous.intersectsAt(current);
        offsetLines[index-1].endPoint = new Point(intersection);
        offsetLines[index].startPoint = new Point(intersection);
      }
    }
    return offsetLines;
  }
}
