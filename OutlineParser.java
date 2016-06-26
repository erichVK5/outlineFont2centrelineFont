import java.util.ArrayList;

public class OutlineParser {

  // this utility has been designed with ttf2svg SVG exported
  // ttf font paths in mind, and ttf2svg seems pretty well
  // behaved with the path definitions, using only M,L,Q,T,Z
  // and integers for coordinate values
  // but we'll define the other accepted things anyway

  int pathIndex = 0;

  // we expect to be passed a single outline's M->Z path,
  // not a set of d={M->ZM->ZM->Z}
  public OutlineElement[] outlineParserFunction(String path) {
    // we assume there are less than 100 elements in the outline for now
    OutlineElement [] returnedElements = new OutlineElement [300];
    int elementIndex = 0;
    long startX = 0;
    long startY = 0;
    long previousX = 0;
    long previousY = 0;
    long currentX = 0;
    long currentY = 0;
    long nextX = 0;
    long nextY = 0;
    long control1X = 0;
    long control1Y = 0;

    char currentChar = ' ';
    char lastChar = ' ';

    int commandCount = 0;

    // System.out.println(path);
    if (path.charAt(0) != 'M') {
      System.out.println("The path seems to have an invalid start.");
    } else {
      while (pathIndex < (path.length()-1)) {
        currentChar = path.charAt(pathIndex);
        if (currentChar == 'M') {
          startX = currentX = getNextCoord(path);
          startY = currentY = getNextCoord(path);
          lastChar = currentChar;
          commandCount++;
        } else if (currentChar == 'm') {
          if (commandCount == 0) { // first 'm' treated as absolute
            startX = currentX = getNextCoord(path);
            startY = currentY = getNextCoord(path);
          } else { // relative thereafter
            currentX = currentX + getNextCoord(path);
            currentY = currentY + getNextCoord(path);
          }
          lastChar = currentChar;
          commandCount++;
        } else if ((currentChar == 'Z') || (currentChar == 'z')) {
          returnedElements[elementIndex++] =
              new Line(currentX, currentY, startX, startY);
          previousX = currentX;
          previousY = currentY;
          lastChar = currentChar;
          commandCount++;
        } else if (currentChar == 'L') {
          nextX = getNextCoord(path);
          nextY = getNextCoord(path);
          returnedElements[elementIndex] =
              new Line(currentX, currentY, nextX, nextY);
          previousX = currentX;
          previousY = currentY;
          currentX = nextX;
          currentY = nextY;
          elementIndex++;
          lastChar = currentChar;
          commandCount++;
        } else if (currentChar == 'l') {
          nextX = currentX + getNextCoord(path);
          nextY = currentY + getNextCoord(path);
          returnedElements[elementIndex] =
              new Line(currentX, currentY, nextX, nextY);
          previousX = currentX;
          previousY = currentY;
          currentX = nextX;
          currentY = nextY;
          elementIndex++;
          lastChar = currentChar;
          commandCount++;
        } else if (currentChar == 'H') {
          nextX = getNextCoord(path);
          returnedElements[elementIndex] =
              new Line(currentX, currentY, nextX, currentY);
          previousX = currentX;
          currentX = nextX;
          elementIndex++;
          lastChar = currentChar;
          commandCount++;
        } else if (currentChar == 'h') {
          nextX = currentX + getNextCoord(path);
          returnedElements[elementIndex] =
              new Line(currentX, currentY, nextX, currentY);
          previousX = currentX;
          currentX = nextX;
          elementIndex++;
          lastChar = currentChar;
          commandCount++;
        } else if (currentChar == 'V') {
          nextY = getNextCoord(path);
          returnedElements[elementIndex] =
              new Line(currentX, currentY, currentX, nextY);
          previousY = currentY;
          currentY = nextY;
          //previousY = currentY;
          elementIndex++;
          lastChar = currentChar;
          commandCount++;
        } else if (currentChar == 'v') {
          nextY = currentY + getNextCoord(path);
          returnedElements[elementIndex] =
              new Line(currentX, currentY, currentX, nextY);
          previousY = currentY;
          currentY = nextY;
          //previousY = currentY;
          elementIndex++;
          lastChar = currentChar;
          commandCount++;
        } else if (currentChar == 'Q') {
          control1X = getNextCoord(path);
          control1Y = getNextCoord(path);
          nextX = getNextCoord(path);
          nextY = getNextCoord(path);
          returnedElements[elementIndex] =
              new Quadratic(currentX, currentY, control1X, control1Y, nextX, nextY);
          //System.out.println("% Just parsed a quadratic");
          previousX = currentX;
          previousY = currentY;
          currentX = nextX;
          currentY = nextY;
          elementIndex++;
          lastChar = currentChar;
          commandCount++;
        } else if (currentChar == 'q') {
          control1X = currentX + getNextCoord(path);
          control1Y = currentY + getNextCoord(path);
          nextX = control1X + getNextCoord(path);
          nextY = control1Y + getNextCoord(path);
          returnedElements[elementIndex] =
              new Quadratic(currentX, currentY, control1X, control1Y, nextX, nextY);
          //System.out.println("% Just parsed a quadratic");
          previousX = currentX;
          previousY = currentY;
          currentX = nextX;
          currentY = nextY;
          elementIndex++;
          lastChar = currentChar;
          commandCount++;
        } else if (currentChar == 'T') {
          // now, we need to calculate the next lot of control points
          // which are mirrored, based on the previous ones.
          // vector addition seems easiest...
          Line newCPAsVector
              = new Line(control1X, control1Y, currentX, currentY);
          Point currentP = newCPAsVector.endPoint;
          Point newCP = currentP.plus(newCPAsVector); 
          control1X = newCP.getX();
          control1Y = newCP.getY();
          if ((lastChar != 'Q')
              && (lastChar != 'T')
              && (lastChar != 'q')
              && (lastChar != 't')) {
            control1X = currentX;
            control1Y = currentY;
          }
          nextX = getNextCoord(path);
          nextY = getNextCoord(path);
          returnedElements[elementIndex] =
              new Quadratic(currentX, currentY, control1X, control1Y, nextX, nextY);
          //System.out.println("% Just parsed a symmetric quadratic");
          previousX = currentX;
          previousY = currentY;
          currentX = nextX;
          currentY = nextY;
          elementIndex++;
          lastChar = currentChar;
          commandCount++;
        } else if (currentChar == 't') {
          // now, we need to calculate the next lot of control points
          // which are mirrored, based on the previous ones.
          // vector addition seems easiest...
          Line newCPAsVector
              = new Line(control1X, control1Y, currentX, currentY);
          Point currentP = newCPAsVector.endPoint;
          Point newCP = currentP.plus(newCPAsVector); 
          control1X = currentX + newCP.getX();  // not tested
          control1Y = currentY + newCP.getY();  // not tested
          if ((lastChar != 'Q')
              && (lastChar != 'T')
              && (lastChar != 'q')
              && (lastChar != 't')) {
            control1X = currentX;  // not tested
            control1Y = currentY;  // not tested
          }
          nextX = currentX + getNextCoord(path); // not tested
          nextY = currentY + getNextCoord(path); // not tested
          returnedElements[elementIndex] =
              new Quadratic(currentX, currentY, control1X, control1Y, nextX, nextY);
          //System.out.println("% Just parsed a symmetric quadratic");
          previousX = currentX;
          previousY = currentY;
          currentX = nextX;
          currentY = nextY;
          elementIndex++;
          lastChar = currentChar;
          commandCount++;
        }
      }
      if (elementIndex > 0) {
        OutlineElement[] tempArray = new OutlineElement[elementIndex];
        for (int index = 0; index < elementIndex; index++) {
          tempArray[index] = returnedElements[index];
        }
        returnedElements = tempArray;
      }
    }
    pathIndex = 0;
    return returnedElements;
  }

  public ArrayList<OutlineElement> outlineParserToArrayList(String path) {
    OutlineElement[] tempArray = outlineParserFunction(path);
    ArrayList<OutlineElement> retList = new ArrayList<OutlineElement>();
    for (int index = 0; index < tempArray.length; index++) {
      retList.add(tempArray[index]);
    }
    return retList;
  }

  // we use this routine to extract the coord
  private long getNextCoord(String thePath) {
    pathIndex++;
    int startIndex = pathIndex;
    while (thePath.charAt(pathIndex) != ' ' &&
           thePath.charAt(pathIndex) != '\n' && //hopefully a non issue
           thePath.charAt(pathIndex) != '\r' && //hopefully a non issue
           thePath.charAt(pathIndex) != '\t' && //hopefully a non issue
           thePath.charAt(pathIndex) != 'M' &&
           thePath.charAt(pathIndex) != 'H' && // try this
           thePath.charAt(pathIndex) != 'V' && // try this
           thePath.charAt(pathIndex) != 'L' &&
           thePath.charAt(pathIndex) != 'Z' &&
           thePath.charAt(pathIndex) != 'T' &&
           thePath.charAt(pathIndex) != 'Q' &&
           thePath.charAt(pathIndex) != 'm' &&
           thePath.charAt(pathIndex) != 'h' &&
           thePath.charAt(pathIndex) != 'v' &&
           thePath.charAt(pathIndex) != 'l' &&
           thePath.charAt(pathIndex) != 'z' &&
           thePath.charAt(pathIndex) != 't' &&
           thePath.charAt(pathIndex) != 'q' &&
           pathIndex != (thePath.length()-1)) {
      pathIndex++;
    }
    String newCoord = "";
    if (pathIndex == thePath.length()-1) {
      newCoord = thePath.substring(startIndex);
      pathIndex++;
    } else {
      newCoord = thePath.substring(startIndex, pathIndex);
    }
    //System.out.println("%New Coord: " + newCoord);
    return Long.parseLong(newCoord);
  }

}
