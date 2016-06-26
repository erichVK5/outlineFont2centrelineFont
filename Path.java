import java.util.ArrayList;
import java.util.List;

public class Path {

  long length = 0;

  ArrayList<OutlineElement> elements = new ArrayList<OutlineElement>();

  Point start = null;
  Point end = null;
  private int originalDirection = 0;

  public Path(String SVGPath, String type) {// can extend for DXF etc..
    if (type.equals("svg") || type.equals("SVG") ) {
      OutlineParser OP = new OutlineParser();
      elements.addAll(OP.outlineParserToArrayList(SVGPath));
      if (!elements.isEmpty()) {
        start = elements.get(0).start();
        start = elements.get(elements.size()-1).end();
      }
    }
    originalDirection = pathDirection();
  }

  public Path(Path other) {
    for (OutlineElement el : other.elements) {
      elements.add(el.copyOf());
    }
    if (!elements.isEmpty()) {
      start = elements.get(0).start();
      start = elements.get(elements.size()-1).end();
    }
    originalDirection = other.originalDirection();
  }

  public Path(OutlineElement[] lineArray) {
    //System.out.println("lineArray size:" + lineArray.length);
    for (OutlineElement el : lineArray) {
      // System.out.println("Adding copy of element" + el);
      if (true) { //el != null) {
        elements.add(el);
      }
    }
    /*System.out.println("finished creating path, about to "
                       + "set start, end points of path");
    System.out.println("elements ArrayList.size() = "
    + elements.size()); */
    if (!elements.isEmpty()) {
      //System.out.println("start point is: "
      //                + elements.get(0).start());
      start = elements.get(0).start();
      //System.out.println("end point is: "
      //                  + elements.get(elements.size()-1).end());
      end = elements.get(elements.size()-1).end();
    }
    //System.out.println("path constructor "+
    //                   " 'Path(OutlineElement[] lineArray)' all done");
    originalDirection = pathDirection();
  }

  public Path(ArrayList<OutlineElement> lineArrayList) {
    for (OutlineElement el : lineArrayList) {
      elements.add(el.copyOf());
    }
    if (!elements.isEmpty()) {
      start = elements.get(0).start();
      end = elements.get(elements.size()-1).end();
    }
    originalDirection = pathDirection();
  }

  public ArrayList<Line> stitchLines() { // not used currently
    ArrayList<Line> tempLines
        = new ArrayList<Line>();
    for (OutlineElement el : elements) {
      Line copied = (Line)el.copyOf();
      if (el.isBezierSegment()) {
        copied.setAsBezierSegment();
      }
      tempLines.add(copied); // create copies we can modify
    }
    for (int index = 1; index < tempLines.size(); index++) {
      Line spanLine
          = new Line(tempLines.get(index-1).end(),
                     tempLines.get(index).start());
      Point intersection
          = spanLine.midPoint();
      Point newEndPoint = new Point(intersection);
      Point newStartPoint = new Point(intersection);
      if (true || (tempLines.get(index-1).isBezierSegment() //broken
                   && tempLines.get(index).isBezierSegment())) {
        tempLines.get(index-1).newEnd(newEndPoint);
        tempLines.get(index).newStart(newStartPoint);
      }
    }
    return tempLines;
  }

  public Path censorDuplicateCollinearLines(int limbThickness) {
    ArrayList<OutlineElement> tempLines
        = new ArrayList<OutlineElement>();
    for (OutlineElement el : elements) {
      Line copied = (Line)el.copyOf();
      if (el.isBezierSegment()) {
        copied.setAsBezierSegment(); //probably not needed
      }
      tempLines.add(copied); // create copies we can modify
    }
    for (int index = 1; index < tempLines.size(); index++) {
      System.out.println("Testing for collinearity...");
      if (tempLines.get(index-1).isNearlyCollinear((Line)tempLines.get(index), limbThickness)
          && !(tempLines.get(index-1).isRedundant() //trying this
               || tempLines.get(index).isRedundant())) { //and this) {
        if (tempLines.get(index-1).length()
            < tempLines.get(index).length()) {
          tempLines.get(index-1).makeRedundant();
          System.out.println("Making first line redundant...");
        } else {
          tempLines.get(index).makeRedundant();
          System.out.println("Making second line redundant...");
        }
      }
    }

    if (tempLines.size() > 1) { // now sort out first and last segments
      System.out.println("Testing for collinearity...");

      if (tempLines.get(tempLines.size()-1).isNearlyCollinear((Line)tempLines.get(0), limbThickness)
          && !(tempLines.get(tempLines.size()-1).isRedundant() //trying this
               || tempLines.get(0).isRedundant())) { //and this) {
        if (tempLines.get(tempLines.size()-1).length()
            < tempLines.get(0).length()) {
          tempLines.get(tempLines.size()-1).makeRedundant();
          System.out.println("Making first line redundant...");
        } else {
          tempLines.get(0).makeRedundant();
          System.out.println("Making second line redundant...");
        }
      }
    }
    return new Path(tempLines);
  }

  public Path stitchCloseSequentialLines(int limbThickness) {
    ArrayList<OutlineElement> tempLines
        = new ArrayList<OutlineElement>();
    for (OutlineElement el : elements) {
      Line copied = (Line)el.copyOf();
      if (el.isBezierSegment()) {
        copied.setAsBezierSegment(); //probably not needed
      }
      tempLines.add(copied); // create copies we can modify
    }
    for (int index = 1; index < tempLines.size(); index++) {
      Line spanLine
          = new Line(tempLines.get(index-1).end(),
                     tempLines.get(index).start());
      if (spanLine.length() >= 1) { // consecutive lines not joined
        Point intersectionA // not consecutive beziers with gap
            = ((Line)tempLines.get(index)).intersectsAt((Line)tempLines.get(index-1));
        if (!tempLines.get(index-1).isBezierSegment()
            && !tempLines.get(index).isBezierSegment()
            && (spanLine.length() < limbThickness)
            && (spanLine.length() < limbThickness)
            && !(tempLines.get(index-1).isOppositeLimbEdge((Line)tempLines.get(index), limbThickness))) { // avoids odd terminations of limbs
          //consecutive lines/not bezier segments
          Point newEndPoint = new Point(intersectionA);
          Point newStartPoint = new Point(intersectionA);
          tempLines.get(index-1).newEnd(newEndPoint);
          tempLines.get(index).newStart(newStartPoint);
        } 
      }
    }
    if (tempLines.size() > 1) { // now stitch first and last segments
      Line spanLine
          = new Line(tempLines.get(0).start(),
                     tempLines.get(tempLines.size()-1).end());
      if (spanLine.length() >= 1) { // consecutive lines not joined
        Point intersectionA // not consecutive beziers with gap
            = ((Line)tempLines.get(0)).intersectsAt((Line)tempLines.get(tempLines.size()-1));
        if (!tempLines.get(0).isBezierSegment()
            && !tempLines.get(tempLines.size()-1).isBezierSegment()
            && (spanLine.length() < limbThickness)) {
          //consecutive lines/ not bezier segments
          Point newEndPoint = new Point(intersectionA);
          Point newStartPoint = new Point(intersectionA);
          tempLines.get(tempLines.size()-1).newEnd(newEndPoint);
          tempLines.get(0).newStart(newStartPoint);
        } 
      }
    }
    return new Path(tempLines);
  }


  public Path stitchCloseSequentialBeziers(int limbThickness) {
    ArrayList<OutlineElement> tempLines
        = new ArrayList<OutlineElement>();
    for (OutlineElement el : elements) {
      Line copied = (Line)el.copyOf();
      if (el.isBezierSegment()) {
        copied.setAsBezierSegment();  //probably not needed
      }
      tempLines.add(copied); // create copies we can modify
    }
    for (int index = 1; index < tempLines.size(); index++) {
      Line spanLine
          = new Line(tempLines.get(index-1).end(),
                     tempLines.get(index).start());
      if (spanLine.length() >= 1) { // consecutive lines not joined
        Point intersectionA // not consecutive beziers with gap
            = ((Line)tempLines.get(index)).intersectsAt((Line)tempLines.get(index-1));
        Point intersectionB // consecutive beziers with gap
            = spanLine.midPoint();
        if (tempLines.get(index-1).isBezierSegment()
            && tempLines.get(index).isBezierSegment()
            && spanLine.length() < limbThickness ) {
          Point newEndPoint = new Point(intersectionB);
          Point newStartPoint = new Point(intersectionB);
          tempLines.get(index-1).newEnd(newEndPoint);
          tempLines.get(index).newStart(newStartPoint);
        } else if (!tempLines.get(index-1).isBezierSegment()
            && tempLines.get(index).isBezierSegment()
            && spanLine.length() < limbThickness ) {
          Point newStartPoint = new Point(tempLines.get(index-1).end());
          tempLines.get(index).newStart(newStartPoint);
        } else if (tempLines.get(index-1).isBezierSegment()
            && !tempLines.get(index).isBezierSegment()
            && spanLine.length() < limbThickness ) {
          Point newEndPoint = new Point(tempLines.get(index).start());
          tempLines.get(index-1).newEnd(newEndPoint);
        } else if  (false) { //(spanLine.length() < limbThickness ) {
          //consecutive lines/not bezier segments
          Point newEndPoint = new Point(intersectionA);
          Point newStartPoint = new Point(intersectionA);
          tempLines.get(index-1).newEnd(newEndPoint);
          tempLines.get(index).newStart(newStartPoint);
        } 
      }
    }
    if (tempLines.size() > 1) { // now stitch first and last segments
      Line spanLine
          = new Line(tempLines.get(0).start(),
                     tempLines.get(tempLines.size()-1).end());
      if (spanLine.length() >= 1) { // consecutive lines not joined
        Point intersectionA // not consecutive beziers with gap
            = ((Line)tempLines.get(0)).intersectsAt((Line)tempLines.get(tempLines.size()-1));
        Point intersectionB // consecutive beziers with gap
            = spanLine.midPoint();
        if (tempLines.get(tempLines.size()-1).isBezierSegment()
            && tempLines.get(tempLines.size()-1).isBezierSegment()
            && spanLine.length() < limbThickness ) {
          Point newEndPoint = new Point(intersectionB);
          Point newStartPoint = new Point(intersectionB);
          tempLines.get(tempLines.size()-1).newEnd(newEndPoint);
          tempLines.get(0).newStart(newStartPoint);
        } else if (!tempLines.get(tempLines.size()-1).isBezierSegment()
            && tempLines.get(0).isBezierSegment()
            && spanLine.length() < limbThickness ) {
          Point newStartPoint
              = new Point(tempLines.get(tempLines.size()-1).end());
          tempLines.get(0).newStart(newStartPoint);
        } else if (tempLines.get(tempLines.size()-1).isBezierSegment()
            && !tempLines.get(0).isBezierSegment()
            && spanLine.length() < limbThickness ) {
          Point newEndPoint = new Point(tempLines.get(0).start());
          tempLines.get(tempLines.size()-1).newEnd(newEndPoint);
        } else if (false) { //(spanLine.length() < limbThickness ) {
          //consecutive lines/ not bezier segments
          Point newEndPoint = new Point(intersectionA);
          Point newStartPoint = new Point(intersectionA);
          tempLines.get(tempLines.size()-1).newEnd(newEndPoint);
          tempLines.get(0).newStart(newStartPoint);
        } 
      }
    }
    return new Path(tempLines);
  }

  public ArrayList<Line> joinBezierSegments() {
    ArrayList<Line> tempLines
        = new ArrayList<Line>();
    for (OutlineElement el : elements) {
      tempLines.add(((Line)el).copyOf()); // create a copy we can then modify
    }
    for (int index = 1; index < tempLines.size(); index++) {
      if (tempLines.get(index).isBezierSegment()
          && !tempLines.get(index-1).isBezierSegment()) {
        tempLines.get(index).newStart(tempLines.get(index-1).end());
      } else if (tempLines.get(index).isBezierSegment()
          && tempLines.get(index-1).isBezierSegment()) {
        Point intersection
            = ((Line)tempLines.get(index)).intersectsAt((Line)tempLines.get(index-1));
        tempLines.get(index-1).newEnd(intersection);
        tempLines.get(index).newStart(intersection);
      } else if (!tempLines.get(index).isBezierSegment()
          && tempLines.get(index-1).isBezierSegment()) {
        Point intersection
            = ((Line)tempLines.get(index)).intersectsAt((Line)tempLines.get(index-1));
        tempLines.get(index-1).newEnd(intersection);
        tempLines.get(index).newStart(intersection);
      } 
    }
    return tempLines;
  }

  public Line[] joinBezierSegmentsAsArray() {
    ArrayList<Line> temp = joinBezierSegments();
    Line[] retArray = new Line[temp.size()];
    for (int index = 0; index < temp.size(); index++) {
      retArray[index] = (Line)temp.get(index);
    }
    return retArray;
  }

  public OutlineElement[] toElementArray() {
    OutlineElement[] retArray = new OutlineElement[elements.size()];
    for (int i = 0; i < elements.size(); i++) {
      retArray[i] = elements.get(i).copyOf();
    }
    return retArray;
  }

  public int pathDirection() {
    return pathDirection(this);
  }

  public int originalDirection() {
    return originalDirection;
  }

  public void setDirection(int inheritedDirection) {
    originalDirection = inheritedDirection;
  }

  public static int pathDirection(Path p) {
    // CW is -1, CCW is +1 pathDir
    // ttf fonts have outer paths in CW direction
    // and inner paths in CCW direction
    double overallDeltaTheta = 0;
    for (OutlineElement el : p.elements) {
      overallDeltaTheta += el.deltaTheta();
      //System.out.println("__Cumulative path deltaTheta: " 
      //                   + overallDeltaTheta);
    }
    if (overallDeltaTheta > 0) {
      return 1;
    } else if (overallDeltaTheta < 0) {
      return -1;
    } else {
      return 0;
    }
  }

  public Path removeLikelyEndCaps(int limbWidth) {
    ArrayList<OutlineElement> newElements
        = new ArrayList<OutlineElement>();
    for (OutlineElement el : elements) {
      if (!el.likelyEndCap(limbWidth, pathDirection())) {
        newElements.add(el.copyOf()); // create a copy
      }
    }
    Path prunedPath = new Path(newElements);
    prunedPath.setDirection(originalDirection());
    return prunedPath;
  }

  public Path removeLikelySerifs(int limbWidth) {
    ArrayList<OutlineElement> newElements
        = new ArrayList<OutlineElement>();
    for (OutlineElement el : elements) {
      if (!el.likelySerif(limbWidth)) {
        newElements.add(el.copyOf()); // create a copy
      }
    }
    Path prunedPath = new Path(newElements);
    prunedPath.setDirection(originalDirection());
    return prunedPath;
  }

  public Path removeLikelyFillets(int limbWidth) {
    ArrayList<OutlineElement> newElements
        = new ArrayList<OutlineElement>();
    for (OutlineElement el : elements) {
      if (!el.likelyFillet(limbWidth, pathDirection())) {
        newElements.add(el.copyOf()); // create a copy
      }
    }
    Path prunedPath = new Path(newElements);
    prunedPath.setDirection(originalDirection());
    return prunedPath;
  }

  public Path removeSmallVerticals(int limbWidth) {
    ArrayList<OutlineElement> newElements
        = new ArrayList<OutlineElement>();
    for (OutlineElement el : elements) {
      if (!el.smallVertical(limbWidth)) {
        newElements.add(el.copyOf()); // create a copy
      }
    }
    Path prunedPath = new Path(newElements);
    prunedPath.setDirection(originalDirection());
    return prunedPath;
  }

  public Path joinBezierSegmentsAsPath() {
    return new Path(joinBezierSegmentsAsArray());
  }

  public double crudeLength() {
    if (start == null || end == null ) {
      return -1;
    } else {
      return OutlineElement.length(start,end);
    }
  }

  public double length() {
    if (start == null || end == null ) {
      return -1;
    } else {
      double estimatedLength = 0;
      for (OutlineElement segment : elements) {
        estimatedLength += segment.length();
      }
      return estimatedLength;
    }
  }

  public Line[] toLines() {
    ArrayList<Line> tempLines = new ArrayList<Line>();
    for (OutlineElement segment : elements) {
      Line[] tempArray = segment.toLineArray();
      for (int index = 0; index < tempArray.length; index++) {
        tempLines.add(tempArray[index]);
      }
    }
    Line[] tempArray2 = new Line[tempLines.size()];
    for (int index = 0; index < tempArray2.length; index++) {
      tempArray2[index] = tempLines.get(index);
    }
    return tempArray2;
  }

  public String toGEDAElementLines(int limbWidth,
                                   double magnification,
                                   boolean skipRedundantLines,
                                   boolean skipBeziers) {
    Line[] temp = this.toLines();
    String elements = "## Lines Begin:\n";
    boolean inBezier = false;
    for (int i = 0; i < temp.length; i++) {
      if (temp[i].isRedundant()
          && skipRedundantLines) {
        //System.out.println("#Skipping redundant line.");
      } else if (temp[i].isBezierSegment() 
                 && skipBeziers) {
        //System.out.println("#Skipping bezier segment.");
      } else {
        if (temp[i].isBezierSegment()
            && !inBezier){
          elements = elements + "## Bezier begins\n";
              inBezier = true;
        } else if (!temp[i].isBezierSegment()
            && inBezier){
          elements = elements + "## Lines begin\n";
              inBezier = false;
        }
        elements = elements
            + temp[i].toElementLine(limbWidth/10,magnification);
      }
    }
    return elements;
  }

  public String toOctaveLines(boolean skipRedundantLines,
                              boolean skipBeziers) {
    Line[] temp = this.toLines();
    String plots = "";
    for (int i = 0; i < temp.length; i++) {
      if (temp[i].redundantLine
          && skipRedundantLines) {
        System.out.println("%Skipping redundant line.");
      } else if (temp[i].isBezierSegment() 
                 && skipBeziers) {
        System.out.println("%Skipping bezier segment.");
      } else {
        plots = plots
            + temp[i].toOctaveLine();
      }
    }
    return plots;
  }

  public Path generateOffsetPathCW(int limbWidth) {
    ArrayList<OutlineElement> offsetLines
        = new ArrayList<OutlineElement>();
    for (OutlineElement segment : elements) {
      Line[] tempOffsetArray
          = segment.generateCentrelineCWArray(limbWidth);
      for (Line l : tempOffsetArray) {
        offsetLines.add(l);
        //System.out.println("##adding offsetLine to new list...");
        //if (l.isBezierSegment()) {
        //  System.out.println("##and new line is bezierSegment()...");
        //}
      }
    }
    Path tempPath = new Path(offsetLines);    
    return tempPath;
  }
  
  public long guesstimateLimbThickness(Path other) {
    // we note that other could be the same Path, i.e. for letter 'm'
    boolean hasALine = false;
    int firstLine = -1;
    double limbWidth = 100000000; //pick something unlikely
    for (int index = 0; index <  elements.size(); index++) {
      hasALine = elements.get(index).isLine();
      if (hasALine) {
        firstLine = index;
        index = elements.size();
      }
    }
    if (firstLine != -1) {
      Line testLine = new Line((Line)elements.get(firstLine));
      for (int index = 0; index <  other.elements.size(); index++) {
        if (other.elements.get(index).isLine()) {
          if ((testLine.distanceFrom((Line)other.elements.get(index))
               != 0)
              && (testLine.isAntiParallelTo((Line)other.elements.get(index))
                  || (testLine.isParallelTo((Line)other.elements.get(index))))
              && (testLine.distanceFrom((Line)other.elements.get(index))
                  < limbWidth)) {
            limbWidth
                = testLine.distanceFrom((Line)other.elements.get(index));
          }
        }
      }
    } else {
      Line[] firstQuad = elements.get(0).toLineArray();
      Line testLine = new Line(firstQuad[0]);
      for (int index = 0; index <  other.elements.size(); index++) {
        Line[] testQuad = other.elements.get(index).toLineArray();
        for (int j = 0; j < testQuad.length; j++) {
          if ((testLine.distanceFrom(testQuad[j]) != 0)
              && (testLine.isAntiParallelTo(testQuad[j])
                  || testLine.isParallelTo(testQuad[j]))
            && (testLine.distanceFrom(testQuad[j])
                < limbWidth)) {
            limbWidth
                = testLine.distanceFrom(testQuad[j]);
          }
        }
      }
    }    
    if (limbWidth == 100000000) {
      System.out.println("Unable to determine limbWidth.");
      limbWidth = -1;
    }      
    return (long)limbWidth;
  }
}
