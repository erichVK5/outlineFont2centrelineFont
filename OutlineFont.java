import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;

public class OutlineFont {

  public ArrayList<Glyph> glyphList = new ArrayList<Glyph>();

  public long fontAscent = 0L;
  public long fontDescent = 0L;
  private Glyph newGlyph;

  public OutlineFont(String SVGFontDefs) {
    Scanner svg = new Scanner(SVGFontDefs);
    String currentLine = "";
    String currentGlyph ="";
    while (svg.hasNextLine()) {
      currentLine = svg.nextLine();
      if (currentLine.contains("ascent=")) {
        fontAscent = extractAscent(currentLine);
      } else if (currentLine.contains("descent=")) {
        fontDescent = extractDescent(currentLine);
      } else if (currentLine.startsWith("<glyph")) {
        currentGlyph = currentLine;
        if (!currentLine.endsWith("/>")) {
          currentLine = svg.nextLine();
          while (!currentLine.endsWith("/>")
                 && svg.hasNextLine()) {
            currentGlyph = currentGlyph + " " + currentLine;
            currentLine = svg.nextLine();
          }
          currentGlyph = currentGlyph + " " + currentLine;
        }
        ArrayList<String> subPaths
            = extractSVGPaths(currentGlyph);
        //for (String subpath : subPaths) {
        //  System.out.println("About to give glyph constructor: " +
        //                     subpath);          
        //}
        newGlyph = new Glyph(subPaths);
        newGlyph.setAscentDescent(fontAscent,fontDescent);
        newGlyph.setGlyphName(extractGlyphName(currentGlyph));
        newGlyph.setHorizAdvance(extractHorizAdvX(currentGlyph));
        //System.out.println("About to add newly constructed Glyph to font");
        glyphList.add(newGlyph);
      } 
    }
    //System.out.println(glyphList.size() + " glyphs added to Font");
    //System.out.println("Font ascent:  " + fontAscent);
    //System.out.println("Font descent: " + fontDescent);
  }

  public double gEDAScaling() {
    double retval = 1.0;
    if ((fontAscent > 0) || (fontDescent < 0)) {
      retval = 6333.0/(fontAscent + fontDescent); // not minus
      // PCB fonts are about 6333 high, offset +1000 to the right,
      // and sit at ~ 1000 Y 
    }
    return retval; // default is 1.0 if ascent, descent not set.
  }

  public double fontLimbWidth() {
    ArrayList<Double> estimates = new ArrayList<Double>();
    
    for (Glyph g : glyphList) {
      Double glyphEstimate = g.limbThicknessGuess();
      if (glyphEstimate != -1) { // glyph checks size vs ascent/descent
        estimates.add(glyphEstimate);
      }
    }
    //System.out.println("Font's limbWidth estimates:");
    Collections.sort(estimates);
    Double average = 0.0;
    for (Double est : estimates) {
      //System.out.println("limbWidth: " + est);
      average += est;
    }
    Double ave = (double)(average)/estimates.size();
    //System.out.println("limbWidth estimate average: " + ave);
    int listSize = estimates.size();
    double medianThickness = 0;
    if (listSize%2 == 1) {
      medianThickness = estimates.get(listSize/2);
      //System.out.println("limbWidth median: "
      //                   + estimates.get(listSize/2));
    } else if (listSize != 0) { // i.e. even number of estimates
      medianThickness = (estimates.get(listSize/2 - 1)
                         + estimates.get(listSize/2))/2;
      //System.out.println("limbWidth median: "
      //                   + (double)(estimates.get(listSize/2)
      //                   + estimates.get(listSize/2 - 1))/2);
    }
    //System.out.println("Font limbWidth median: " + medianThickness);
    return medianThickness;
  }

  public int fontGlyphCount() {
    return glyphList.size();
  }

  public Glyph provideGlyphNumber(int theGlyph) {
    return glyphList.get(theGlyph);
  }
 
  public String toString() {
    String returnString = "";
    for (Glyph g : glyphList) {
      returnString = returnString + g + "\n";
    }
    return returnString;
  }

  private ArrayList<String> extractSVGPaths(String glyph) {
    String temp = glyph;
    int index = 0;
    //System.out.println("Paths 'temp' before OutlineFont tokenises: " + 
    //                     temp );
    index = temp.indexOf("d=\"");
    temp = temp.substring(index);
    index = temp.indexOf("Z\" "); // we may still have multiple paths
    temp = temp.substring(3,index);
    ArrayList<String> retList = new ArrayList<String>();
    String[] tokenisedByZ = temp.split("Z");
    //System.out.println("Paths 'temp' just before OutlineFont tokenises: " + 
    //                     temp );
    for (String str : tokenisedByZ) {
      //System.out.println("New path from OutlineFont tokenising: " + 
      //                   str );
      retList.add(str);
    }
 
    return retList;
  }

  private String extractGlyphName(String glyph) {
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

  private long extractAscent(String line) {
    //System.out.println("About to find ascent in: " + line);
    int index = line.indexOf("cent=\"");
    String temp = line.substring(index+6);
    //System.out.println("Extracting ascent from: " + temp);
    index = temp.indexOf("\"");
    return Long.parseLong(temp.substring(0,index));
  }

  private long extractDescent(String line) {
    return extractAscent(line); // can use same code
  }

  private int extractHorizAdvX(String glyph) {
    int index = glyph.indexOf("horiz-adv-x=\"");
    String temp = glyph.substring(index);
    index = temp.indexOf("\" ");
    return Integer.parseInt(temp.substring(13,index));
  }
}
