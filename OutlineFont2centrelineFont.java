import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Collections;


public class OutlineFont2centrelineFont {

  public static void main(String [] args) throws IOException {
    double theta = 0;
    long radius = 300;
    long thickness = 80;//40; for osifont
    long currentX = radius;
    long currentY = 0;
    long nextX = 0;
    long nextY = 0;
    Line test;
    Line offset;
    Line tempOffset;
    Line oldOffset = new Line(0,0,0,0);
    Line trimmedOffset;
    Point oldStart;
    Point oldEnd;
    String plots = "";
    int numLines = 16;
    Line [] lineArray = new Line[numLines];
    Line [] offsetLineArray = new Line[numLines];

    boolean aggressive = false;
    boolean skippingBeziers = false;
    boolean suppressSerifs = false;
    boolean suppressEndcaps = false;
    boolean suppressFillets = false;
    boolean suppressSmallVerticals = false;
    boolean outLineOnly = false;
    boolean guessThickness = false;
    boolean centreLine = false;
    boolean outerPathsOnly = false;
    boolean innerPathsOnly = false;

    String fontAscent = ""; // integer value not actually used for now
    String fontDescent = ""; // integer value not actually used for now

    int glyphNumber = 0;
    String filename = "";
    int pathToDo = -1;
    int limbWidth = 0; // can now autodetect 70; // 150 for osifont, 70 for hebrew miriam

    int firstGlyph = 0;
    int lastGlyph = 0; // which glyphs to do in the svg file

    boolean autoMagnify = true;
    double magnification = 1.0;// can use ascent, descent for scaling

    ArrayList<String> glyphList= new ArrayList<String>();

    for (int index = 0; index < args.length; index++) {
      if (args[index].equals("-s")) {
        filename = args[index+1];
        index++;
      } else if (args[index].equals("-g")) { //glyph number
        glyphNumber = Integer.parseInt(args[index+1]);
        index++;
      } else if (args[index].equals("-w")) { //limb width
        limbWidth = Integer.parseInt(args[index+1]);
        index++; // default 70 for now
      } else if (args[index].equals("-l")) { //limb width
        firstGlyph = Integer.parseInt(args[index+1]);
        index++; // default 70 for now
      } else if (args[index].equals("-h")) { //limb width
        lastGlyph = Integer.parseInt(args[index+1]);
        index++; // default 70 for now
      } else if (args[index].equals("-cl")) { //plot centreline
        centreLine = true;
      } else if (args[index].equals("-sa")) { //suppress aggressively
        aggressive = true;
      } else if (args[index].equals("-se")) { //suppress endcaps
        suppressEndcaps = true;
      } else if (args[index].equals("-ss")) { //suppress serifs
        suppressSerifs = true;
      } else if (args[index].equals("-sf")) { //suppress serifs
        suppressFillets = true;
      } else if (args[index].equals("-de")) { // draw everything
        aggressive = false;
      } else if (args[index].equals("-sb")) { //suppress beziers
        skippingBeziers = true;
      } else if (args[index].equals("-ssv")) { 
        suppressSmallVerticals = true; //suppress small verticals
      } else if (args[index].equals("-oo")) { //draw outline only
        outLineOnly = true;
      } else if (args[index].equals("-op")) { // outer paths only
        outerPathsOnly = true;
      } else if (args[index].equals("-ip")) { // inner paths only
        innerPathsOnly = true;
      } else if (args[index].equals("-nm")) { //no auto magnification
        autoMagnify = false;
      } else if (args[index].equals("-p")) { // glyph path number to do
        pathToDo = Integer.parseInt(args[index+1]); // for skipping
        index++;                            // paths in glyph def
      } else if (args[index].equals("-gt")) { // try to figure out
        guessThickness = true; // limb thickness by analysing glyphs
      }
    }
    if (filename.equals("")) {
      System.out.println("No file to work with");
      System.exit(0);
    }

    File SVGFile = new File(filename);
    Scanner svg = new Scanner(SVGFile);
    String currentLine = "";
    String currentGlyph ="";

    //...
    String totalSVG = "";
    //..

    while (svg.hasNextLine()) {
      currentLine = svg.nextLine();
      totalSVG = totalSVG +
          "\n" + currentLine; // for testing font class
      if (currentLine.contains("ascent=")) {
        fontAscent = extractAscent(currentLine);
      } else if (currentLine.contains("descent=")) {
        fontDescent = extractDescent(currentLine);
      } else if (currentLine.startsWith("<glyph")) {
        currentGlyph = currentLine;
        //totalSVG = totalSVG + "\n" + 
        //    currentLine; // for testing font class
        if (!currentLine.endsWith("/>")) {
          currentLine = svg.nextLine();
          //totalSVG = totalSVG + 
          //    "\n" + currentLine; // for testing font class
          while (!currentLine.endsWith("/>")
                 && svg.hasNextLine()) {
            currentGlyph = currentGlyph + " " + currentLine;
            totalSVG = totalSVG +
                "\n" + currentLine; // for testing font class
            currentLine = svg.nextLine();
          }
          currentGlyph = currentGlyph + " " + currentLine;
          totalSVG = totalSVG +
              "\n" + currentLine; // for testing font class
        }
        glyphList.add(currentGlyph);
      } 
    }  

    OutlineFont testingFont = new OutlineFont(totalSVG); // for testing font class

    // summary stats for Font:
    System.out.println("Glyphs contained in font:\n"
                       + testingFont);

 
    System.out.println("Font object's limb width: " + 
                       testingFont.fontLimbWidth());
    if (limbWidth == 0) {
      limbWidth = (int)testingFont.fontLimbWidth();
    }

    System.out.println("gEDA PCB font scaling:\n"
                       + testingFont.gEDAScaling());
    if (autoMagnify) {  // i.e. -nm not used for "not magnifcation"
        magnification = testingFont.gEDAScaling();
        // PCB fonts are about 6333 high, offset +1000 to the right,
        // and sit at ~ 1000 Y 
    }


    Glyph theGlyph = testingFont.provideGlyphNumber(glyphNumber);

    String output = "Element[\"\" \""
        + theGlyph.glyphName() 
        + "\" \"\" \"\" 0 0 0 -4000 0 100 \"\"]\n(\n";

    String outputHeader = output;

    OutlineParser OLP4;

    ArrayList<Path> glyphPaths = theGlyph.pathList();
    ArrayList<Integer> outerPaths = new ArrayList<Integer>();
    ArrayList<Integer> innerPaths = new ArrayList<Integer>();
    // here we sort paths by path direction
    // ttf fonts use CW for outer, CCW for inner
    for (int i = 0; i < glyphPaths.size(); i++) {
      if (glyphPaths.get(i).originalDirection() <= 0) {
        System.out.println("Found an outer path");
        outerPaths.add(i);
      } else {
        System.out.println("Found an inner path");
        innerPaths.add(i);
      }
    }

    String workingPathOutput = "";

    for (int j : outerPaths) {
      Path workingPath = new Path(glyphPaths.get(j));
      if (aggressive || suppressEndcaps) {
        Path prunedPath
            = workingPath.removeLikelyEndCaps(limbWidth);
        workingPath = prunedPath;
      }
      if (aggressive || suppressSerifs) {
        Path prunedPath
            = workingPath.removeLikelySerifs(limbWidth);
        workingPath = prunedPath;
      }
      if (aggressive || suppressFillets) {
        Path prunedPath
            = workingPath.removeLikelyFillets(limbWidth);
        workingPath = prunedPath;
      }
      if (aggressive || suppressSmallVerticals) {
        Path prunedPath
            = workingPath.removeSmallVerticals(limbWidth);
        workingPath = prunedPath;
      }
      if (centreLine) {
        Path finalPath
            = workingPath.generateOffsetPathCW(limbWidth);
        workingPath = finalPath;
        finalPath
            = workingPath.stitchCloseSequentialBeziers(limbWidth/2);
        workingPath = finalPath;
        finalPath
            = workingPath.stitchCloseSequentialLines(2*limbWidth/3);
        workingPath = finalPath;
        finalPath
            = workingPath.censorDuplicateCollinearLines(limbWidth);
        workingPath = finalPath;
      }

      workingPathOutput
          = workingPathOutput
          + workingPath.toGEDAElementLines(limbWidth,
                                           magnification,
                                           true, // skip redundant
                                           false) // skip beziers
          + "###\n";
    }

    if (!outerPathsOnly) {
      for (int j : innerPaths) {
        Path workingPath = new Path(glyphPaths.get(j));
        if (aggressive || suppressEndcaps) {
          Path prunedPath
              = workingPath.removeLikelyEndCaps(limbWidth);
          workingPath = prunedPath;
        }
        if (aggressive || suppressSerifs) {
          Path prunedPath
              = workingPath.removeLikelySerifs(limbWidth);
          workingPath = prunedPath;
        }
        if (aggressive || suppressFillets) {
          Path prunedPath
              = workingPath.removeLikelyFillets(limbWidth);
          workingPath = prunedPath;
        }
        if (aggressive || suppressSmallVerticals) {
          Path prunedPath
              = workingPath.removeSmallVerticals(limbWidth);
          workingPath = prunedPath;
        }
        if (centreLine) {
          Path finalPath
              = workingPath.generateOffsetPathCW(limbWidth);
          workingPath = finalPath;
          finalPath
              = workingPath.stitchCloseSequentialBeziers(limbWidth/2);
          workingPath = finalPath;
          finalPath
              = workingPath.stitchCloseSequentialLines(2*limbWidth/3);
          workingPath = finalPath;
          finalPath
              = workingPath.censorDuplicateCollinearLines(limbWidth);
          workingPath = finalPath;
        }
        
        workingPathOutput
            = workingPathOutput
            + workingPath.toGEDAElementLines(limbWidth,
                                             magnification,
                                             true, // skip redundant
                                             false) // skip beziers
            + "###\n";
      }
    }


    String finalOutput = outputHeader + workingPathOutput;


    for (int k = 10; k < glyphPaths.size(); k++) {

      OLP4 = new OutlineParser();

      if (outLineOnly) {

        Path testing = glyphPaths.get(k);

        output = output
            + testing.toGEDAElementLines(limbWidth,
                                         magnification,
                                         false,  // aggressive
                                         false); // skip beziers
      }

      OLP4 = new OutlineParser();

      OutlineElement[] tester4 = glyphPaths.get(k).toElementArray();

      /*
      tester4 = OLP4.outlineParserFunction(extractSVGPath(glyphList.get(glyphNumber)).get(k) );
      System.out.println("__Size of tester4 is: " + tester4.length);
      for (int j = 0; j < tester4.length; j++) {
        System.out.println("__array index " + j + " : " + tester4[j]);
      }
      */

      int s2index = 0;
      Line [] segments2 = new Line [400];

      for (int index = 0; index < tester4.length; index++) {
        if (pathToDo != -1) {
          index = pathToDo; // for skipping paths, i.e. inside loops.
        }
        plots = plots + tester4[index].toOctaveLine();
        Line [] segments = tester4[index].toLineArray();
        //......
        // try this, arrayList may be better

        // this is the offset line generating phase, putting all
        // the paths' segments into one array; may be better as list...
        // if they pass the tests for:
        // endcap, length

        if (index < (tester4.length - 1)) {
          for (int offsets = 0; offsets < segments.length; offsets++) {
            Line temp
                = segments[offsets].generateCentrelineCW(limbWidth); //150

            // this is where we look for likely end caps, and little
            // straight bits between them that shouldn't be
            // rendered

            if (index == 0) { // can only look ahead to next element
              if ((!aggressive &&
                  !suppressSerifs) ||
                  !tester4[index].likelyEndCap(limbWidth) // ) { //150
                  //                && !tester4[index+1].likelyEndCap(75)) { //150
                  && !(tester4[index+1].likelyEndCap(limbWidth) // try to catch
                       && segments[offsets].length() < (limbWidth))) {// serif
                segments2[s2index]
                    = segments[offsets].generateCentrelineCW(limbWidth); //150
                s2index++;
                //System.out.println("% temp offset line segment: " + temp);
              }
            } else { // we can inspect prior and next elements
              if ((!aggressive &&
                   !suppressSerifs) ||
                  !tester4[index].likelyEndCap(limbWidth/2) // ) { //150
                  //                && !tester4[index+1].likelyEndCap(75)) { //150
                  && !((tester4[index+1].likelyEndCap(limbWidth/2)//try to catch
                        && segments[offsets].length() < (limbWidth))// serifs
                       || ( segments[offsets].length() < (limbWidth)
                            && tester4[index-1].likelyEndCap(limbWidth)))){
                segments2[s2index]
                    = segments[offsets].generateCentrelineCW(limbWidth); //150
                s2index++;
                //plots = plots + "% inserting some offsets now\n"
                //+ temp.toOctaveLine();
                //System.out.println("% temp offset line segment: " + temp);
              }
            }
            
            
          }
        } else {
          for (int offsets = 0; offsets < segments.length; offsets++) {
            Line temp = segments[offsets].generateCentrelineCW(limbWidth);
            if ((!aggressive &&
                 !suppressSerifs) ||
                !tester4[index].likelyEndCap(limbWidth) // ) { //150
                //                && !tester4[index+1].likelyEndCap(75)) { //150
                && !(tester4[0].likelyEndCap(limbWidth) // try to catch
                     && segments[offsets].length() < (limbWidth))) {
              segments2[s2index]
                  = segments[offsets].generateCentrelineCW(limbWidth);
              s2index++;
            }
          }
        }
        if (pathToDo != -1) {
          index = tester4.length; //skip other remaining paths.
        }

      }

      // now we stitch non beziers together
      
      for (int i = 0; i < s2index; i++) {
        if (i < (s2index - 1)
            && !(segments2[i].isBezierSegment()
                 || segments2[i+1].isBezierSegment())) {
          segments2[i]
              = segments2[i].extendToNearLine(segments2[i+1],limbWidth,0.8);
          segments2[i+1]
              = segments2[i+1].extendToNearLine(segments2[i],limbWidth,0.8);
          //System.out.println("% about to Test for line duplication");
          // reinstated the following
          if (aggressive) {

            if (segments2[i].isNearlyCollinear(segments2[i+1], limbWidth)
                && !(segments2[i+1].redundantLine //trying this
                     || segments2[i].redundantLine)) { //and this) {
              if (segments2[i].length() < segments2[i+1].length()) {
                segments2[i].makeRedundant();
              } else {
                segments2[i+1].makeRedundant();
              }
            }
          }
          // UP TO HERE
        }


        if (i == (s2index - 1)
            && !(segments2[i].isBezierSegment()
                 || segments2[0].isBezierSegment())) {
          segments2[i]
              = segments2[i].extendToNearLine(segments2[0],limbWidth,.8);
          segments2[0]
              = segments2[i].extendToNearLine(segments2[i],limbWidth,.8);

          if (aggressive) {
            // reinstated the following
            if (segments2[i].isAntiParallelTo(segments2[0])
                && !(segments2[0].redundantLine //trying this
                     || segments2[i].redundantLine)) { //and this
              if (segments2[i].length() <= segments2[0].length()) {
                segments2[i].makeRedundant();
              } else {
                segments2[0].makeRedundant();
              }
            }  
            // up to here.
          }

        }  
        
      }

      // we try to identify duplicates

      OutlineElement.flagDuplicateElements(segments2, s2index, limbWidth); //try this

      // we now generate the plotted lines, if not redundant

      for (int i = 0; i < s2index; i++) {
        if (segments2[i].redundantLine) {
          //System.out.println("Skipping redundant line.");
        } else if (segments2[i].isBezierSegment() 
                   && skippingBeziers) {
          //System.out.println("Skipping bezier segment.");
        } else {
          output = output
              + segments2[i].toElementLine(limbWidth,magnification);
          plots = plots + "% inserting some offsets now\n"
              + segments2[i].toOctaveLine();
        }
      }

      
    }

    finalOutput = finalOutput + ")\n";

    if (suppressSmallVerticals) {
      System.out.println("suppressing small verticals");
    } else {
      System.out.println("not suppressing small verticals");
    }

    /*
    output2 = output2 + ")\n";
    output3 = output3 + ")\n";
    output4 = output4 + ")\n";

    output5 = output5 + ")\n";

    output = output + ")\n";
    */

    File out
        = new File(theGlyph.glyphName()
                   + "-hadvx"
                   + theGlyph.horizAdvance()
                   + "-asc"
                   + theGlyph.ascent()
                   + "-desc"
                   + theGlyph.descent()
                   + ".fp");
    PrintWriter fp = new PrintWriter(out);

    fp.println(finalOutput);
    //    fp.println(output);
    fp.close();

    System.out.println("Glyph ascent and descent:"
                       + theGlyph.ascent() + ", "
                       + theGlyph.descent());
    System.out.println("magnification:" + magnification);

  }

  static private ArrayList<String> extractSVGPath(String glyph) {
    String temp = glyph;
    int index = 0;
    index = temp.indexOf("d=\"");
    temp = temp.substring(index);
    index = temp.indexOf("Z\" "); // we may still have multiple paths
    temp = temp.substring(3,index);
    ArrayList<String> retList = new ArrayList<String>();
    while (temp.contains("Z")) {
      index = temp.indexOf("Z");
      retList.add(temp.substring(0,index));
      temp = temp.substring(index+1);
    }
    retList.add(temp);
    return retList;
  }

  static private String extractGlyphName(String glyph) {
    int index = glyph.indexOf("unicode=\"&#x"); 
    String temp = "";
    if (index == -1) {
      index = glyph.indexOf("glyph-name=\""); // osi font
      temp = glyph.substring(index);
      index = temp.indexOf("\" ");
    } else {
      temp = " " + glyph.substring(index); // miriam font
      index = temp.indexOf(";\" ");
    }
    return temp.substring(12,index);
  }

  static private String extractAscent(String line) {
    System.out.println("About to find ascent in: " + line);
    int index = line.indexOf("cent=\"");
    String temp = line.substring(index+6);
    System.out.println("Extracting ascent from: " + temp);
    index = temp.indexOf("\"");
    return temp.substring(0,index);
  }

  static private String extractDescent(String line) {
    return extractAscent(line); // can use same code
  }

  static private int extractHorizAdvX(String glyph) {
    int index = glyph.indexOf("horiz-adv-x=\"");
    String temp = glyph.substring(index);
    index = temp.indexOf("\" ");
    return Integer.parseInt(temp.substring(13,index));
  }


}
