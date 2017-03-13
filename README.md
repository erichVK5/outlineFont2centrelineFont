# outlineFont2centrelineFont
A utility to convert SVG fonts to centreline defined fonts for engraving or PCB/gerber use.

The workflow is a work in progress, and this README is more of an aid to remembering what currently needs to be done than an actual howto for general use.

Nevertheless, the code is now at the point where it can simplfy the job of converting outline defined glyphs to centreline defined glyphs, for use in PCB, or other plotter, eggbot, engraving or similar activities.

As an aside, fonts which are sans serif and of uniform thickness are best suited to conversion to centreline defined fonts. Those which have complex, tapering, or very skinny serifs will be difficult to convert effectively.

First of all, the ttf2svg utility is needed. It's part of the apache Batik stuff.

You then need a truetype font to play with. If it has cubic beziers, it will probably generate garbage, as the parsing has not been set up to deal with cubic beziers currently. Most truetype fonts simply have quadratic beziers. 

You need to know the decimal designation of the unicode glyph or range of glyphs you want to convert, i.e. U+05D0 is 1488, and U+05F4 is 1524.

To extract the glyphs ranging from U+05D0 to U+05F4 inclusive from your truetype font, you do the following:

	ttf2svg MiriamCLM-Book.ttf -l 1488 -h 1524 -o thing.svg	

You want a reasonable number of glyphs in your extract, so that the code can reliably determine the limb widths of the font glyphs, meaning you do not need to figure out by trial and error plus specify it manually.

If you then view thing.svg in a text editor, you'll see a set of paths defined, such as

d="M435 415V80Q435 70 445 70H483Q497 70 503 64T509 44V26Q509 12 503 6T483 0H76Q62 0 56 6T50 26V44Q50 58 56 64T76 70H355Q365 70 365 80V425Q36 5 484 342 507T260 530H98Q84 530 78 536T72 556V574Q72 588 78 594T98 600H250Q351 600 393 558T435 415Z"

Each of these paths defines the outline of the character, and sometimes there will be additional paths for the inside edges of loops in a glyph, i.e. the figure 8 will have three paths. The outer path(s) is clockwise, the inner path(s) counterclockwise.

The utility is used to parse the set of paths and turn each set of paths for a glyph into a gEDA PCB footprint, which can then be inspected in gEDA PCB. One of the tricks to getting the centreline right is to specify the correct width for the limbs of the glyph. This requires trial and error if you don't have enough glyphs for the code to analyse.

This is done with the -w flag, i.e. -w 150  sets the limbwidth to 150 of the SVG "units".

The particular glyph in an svg file can be selected with the -g 0, or -g 1, or -g -n etc... flag  

Other useful flags:

-oo generate outline only. This could in theory be abused to create a board outline from SVG...

-ip use inside paths only

-op use outside paths only

-ssv suppress small verticals

-se suppress endcaps

-ss suppress serifs 

-sf suppress fillets, i.e. small beziers between longer segments

-cl generate a centreline path

-s mySVGfile.svg   tells the code which SVG file to look at

-g X  which glyph in the SVG file to process

-w manually specify limbwidth for centreline path offsets

-ep Y  export polygons only (experimental), with Y offset (typically value 6333 to 7000)

For example:

java OutlineFont2centrelineFont -se -ssv -sf -ss -op -cl -s thing1.svg -g 1

Once the output file footprint has been generated, the easiest way from there is to view it in PCB, i.e. run

	pcb myNewThing.fp &

and then open the same footprint in a text editor. Changes made and saved to the footprint will show up in PCB when it refreshes on noticing a change in the footprint file.

The footprints sections are commented, showing which elements are for simple drawn lines, and which elements represent a quadratic bezier.

Until the code is refined enough to automatically censor overlaid paths from opposite sides of a limb, this has to be done manually, by commenting out sections in the footprint.

Alternatively, you may opt for a more complex footprint, comprised of two overlapping paths, but this is less efficient than a centreline defined glyph.

If further editing is needed, in gEDA PCB, the element can be selected, then "cut to buffer", then "Break buffer into pieces", after which the main task of deleting un-necessary or artefactual line elements, and simplifying joins between lines, can be done.

Once done, the elements are selected, "cut to buffer", "convert to element", and then saved.

This becomes the set of line descriptions for the centreline defined glyph.

It is at this point that the footprint needs to be converted to a gEDA PCB compatible font symbol.....

To be continued...

TODO:

sort out the occasional error with bezier segment stitching, which manifests as a join between bezier segments jumping to the origin (0,0).

automatic censoring of overlaid paths from each side of the limb

distinguishing between, and processing accordingly, glyphs with and without internal contours
