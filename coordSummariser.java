import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Collections;


public class coordSummariser {

  public static void main(String [] args) throws IOException {
    long thickness = 80;//40; for osifont
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

    String fontAscent = ""; // integer value not actually used for now
    String fontDescent = ""; // integer value not actually used for now

    String filename = "";
    int limbWidth = 0; // can now autodetect 70; // 150 for osifont, 70 for hebrew miriam

    int bmpHeight = 128;

    boolean autoMagnify = true;
    double magnification = 1.0;// can use ascent, descent for scaling

    ArrayList<String> glyphList= new ArrayList<String>();

    for (int index = 0; index < args.length; index++) {
      if (args[index].equals("-fp")) {
        filename = args[index+1];
        index++;
      } else if (args[index].equals("-h")) { // png height
        bmpHeight = Integer.parseInt(args[index+1]);
        index++;
      }
    }
    if (filename.equals("")) {
      System.out.println("No file to work with");
      System.exit(0);
    }

    File FPFile = new File(filename);
    Scanner fp = new Scanner(FPFile);
    String currentLine = "";
    String currentGlyph ="";

    long currentX1 = 0;
    long currentY1 = 0;
    long currentX2 = 0;
    long currentY2 = 0;

    long minX = 120000; // should be ~= 1000 
    long minY = 120000; // should be <= 1000
    long maxX = 0;
    long maxY = 0;

    while (fp.hasNextLine()) {
      currentLine = fp.nextLine();
      if (currentLine.startsWith("ElementLine[")) {
	  currentLine = currentLine.substring(12,currentLine.length()-1);
	  //System.out.println(currentLine);
          String[] tokens = currentLine.split(" "); //extract coords
	  currentX1 = Integer.parseInt(tokens[0]);
          currentY1 = Integer.parseInt(tokens[1]);
          currentX2 = Integer.parseInt(tokens[2]);
          currentY2 = Integer.parseInt(tokens[3]);
	  if (currentX1 > maxX) {
	    maxX = currentX1;
	  }
	  if (currentX2 > maxX) {
            maxX = currentX2;
          }
          if (currentX1 < minX) {
            minX = currentX1;
          }
          if (currentX2 < minX) {
            minX = currentX2;
          }
          if (currentY1 > maxY) {
            maxY = currentY1;
          }
          if (currentY2 > maxY) {
            maxY = currentY2;
          }
          if (currentY1 < minY) {
            minY = currentY1;
          }
          if (currentY2 < minY) {
            minY = currentY2;
          }
      }
    }
	
    long deltaX = maxX - minX;
    long deltaY = maxY - minY;
    long pngGlyphHeight = (bmpHeight - 8);
    double scaling = 6333/pngGlyphHeight;

    // summary stats for Fp:
    System.out.println("Summary stats for footprint:\n"
                       + "\tminX: " + minX + "\n"
                       + "\tminY: " + minY + "\n"
                       + "\tmaxX: " + maxX + "\n"
                       + "\tmaxY: " + maxY + "\n"
                       + "\tdeltaX: " + (maxX - minX) + "\n"
                       + "\tdeltaY: " + (maxY - minY) + "\n");
    System.out.println("For " + bmpHeight + " glyph bitmap,\n"
			+ "assume glyph text height (in GIMP) of " + pngGlyphHeight + "\n"
			+ "\tleft x offset = " + minX/scaling + "\n"
                        + "\tnominal y offset = " + (1000/scaling));
 
    fp.close();
  }
}
