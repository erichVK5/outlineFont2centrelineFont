import java.util.ArrayList;
import java.util.Collections;

public class Glyph {

  public ArrayList<Path> paths;
  public long fontAscent = 0L;
  public long fontDescent = 0L;
  public long horizAdvance = 0L;
  public String glyphName = "GlyphNameNotSet";
  private long minX = 0;
  private long maxX = 0;
  private long overallWidth = 0;

  public Glyph(ArrayList<String> SVGPaths) {
    paths = new ArrayList<Path>();
    OutlineParser parser;
    //System.out.println("Glyph constructor provided with paths of size"
    //                   +  SVGPaths.size());
    for (String subPath : SVGPaths) {
      parser = new OutlineParser();
      //System.out.println("About to get me another path, using:" +
      //                   subPath);
      Path newSubPath = new Path(parser.outlineParserFunction(subPath));
      paths.add(newSubPath);
      //System.out.println("Just added latest subpath to Glyph's 'paths'");
    }
    //System.out.println("Glyph constructor all done");
  }

  public Glyph(Glyph other) {
    for (Path p : other.paths) {
      Path pp = new Path(p);
      paths.add(pp);
    }
    fontAscent = other.fontAscent;
    fontDescent = other.fontDescent;
    glyphName = other.glyphName;
  }

  public Glyph copyOf() {
    Glyph newG = new Glyph(this);
    return newG;
  }

  public void setGlyphName(String name) {
    glyphName = name;
  }

  public String glyphName() {
    return glyphName;
  }

  public int glyphPathCount() {
    return paths.size();
  }

  public ArrayList<Path> pathList() {
    ArrayList<Path> retPaths = new ArrayList<Path>();
    for (Path p : paths) {
      Path pp = new Path(p);
      retPaths.add(pp);
    }
    return retPaths;
  }

  public String toString() {
    return "Glyph: " + glyphName + "\t\t fontAscent: " + fontAscent
        + ", fontDesc: " + fontDescent
        + ", path count: " + glyphPathCount();
  }

  public void setAscentDescent(long ascent, long descent){
    fontAscent = ascent;
    fontDescent = descent;
  }

  public void setHorizAdvance(long hadv){
    horizAdvance = hadv;
  }

  public long ascent() {
    return fontAscent;
  }

  public long descent() {
    return fontDescent;
  }

  public long horizAdvance() {
    return horizAdvance;
  }

  public long xOffset(double magnification) {
    glyphWidth(magnification);
    return minX;
  }

  public long glyphWidth(double magnification) {
    minX = 10000000;
    maxX = -10000000;
    for (int j = 0; j < paths.size(); j++) {
      if (minX > paths.get(j).pathMinX(magnification)) {
        minX = paths.get(j).pathMinX(magnification);
      }
      if (maxX < paths.get(j).pathMaxX(magnification)) {
        maxX = paths.get(j).pathMaxX(magnification);
      }      
    }
    overallWidth = maxX-minX;
    return overallWidth;
  }

  public double limbThicknessGuess() {
    ArrayList<Long> estimates = new ArrayList<Long>();
    long guesstimate = 10000000; // unlikely
    for (int j = 0; j < paths.size(); j++) {
      long pathEstimate =
          paths.get(0).guesstimateLimbThickness(paths.get(j));
      //System.out.println("id... Glyph path guesstimate: "
      //                    + pathEstimate);
      if ((pathEstimate > fontAscent/50)
          && (pathEstimate < fontAscent/8)
          && (pathEstimate < guesstimate)) {
        estimates.add(pathEstimate);
      }
    }
    Collections.sort(estimates); // sort to allow median to be found
    //System.out.println("id... final Glyph estimates: ");
    //for (long est : estimates) {
    //  System.out.println("id... estimate: " + est);
    //}
    int listSize = estimates.size();
    if (listSize == 0) {
      return (double)(-1);
    } else if (listSize%2 == 1) { // now find the median value...
      return (double)(estimates.get(listSize/2));
    } else { // which is equivalent to:  if (listSize !=0) {
      return (double)((estimates.get(listSize/2 - 1)
                       + estimates.get(listSize/2))/2);
    }
    
  } 
}
