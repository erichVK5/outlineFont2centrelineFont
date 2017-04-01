# outlineFont2centrelineFont
A utility to 

- convert SVG fonts to centreline defined fonts for engraving or PCB/gerber use.
- convert SVG fonts to polygonal outlines for use in the gEDA PCB fork pcb-rnd's font files (.lht)

The code was originally written with the single purpose of simplifying centreline defined (aka stroked/engraving) font generation from existing truetype fonts, but the recent additional of polygon support within glyphs in the gEDA PCB fork pcb-rnd has led to additional code being added to quickly and easily generate font files with glyphs drawn with polygons.

The code is now at the point where it can simplfy the job of converting outline defined glyphs to centreline defined glyphs, for use in PCB, or other plotter, eggbot, engraving or similar activities, but the workflow is still a bit of a work in progress for centreline/stroked/engraving font generation, so this README is more of an aid to remembering what currently needs to be done than an actual howto for general stroked font conversion.

However, the truetype->polygonal glyph process for converting TTF fonts to gEDA PCB fork pcb-rnd compatible font files works pretty smoothly, i.e. the code can now generate a complete pcb-rnd compatible font file in lihata (.lht) format if asked to. The only things that have to be done manually (until more code is written) to the generated font file full of polygonal glyphs is

- internal polygons need to be combined into external polygon paths. This needs to be automated, but the code is yet to be written.
- glyph names that are not standard ASCII chars, or their escaped equivalents, need to be mapped to an ASCII char. The code will automatically map standard names like "bar" to '|' descriptors, but won;t recognise non-ASCII glyph names


As an aside, fonts which are sans serif and of uniform thickness are best suited to conversion to centreline defined fonts. Those which have complex, tapering, or very skinny serifs will be difficult to convert effectively to a stroked or centreline font.

Whether you're making a pcb-rnd font, or trying to make a stroked font compatible with gEDA PCB and pcb-rnd, the ttf2svg utility is needed. It's part of the apache Batik stuff.

You then need a truetype font to play with. If it has cubic beziers, it will probably generate garbage, as the parsing has not been set up to deal with cubic beziers currently. Most truetype fonts simply have quadratic beziers. Postscript fonts have cubic beziers, apparently.

The other issue which needs to be sorted out is coping with converted truetype fonts which have paths extending into negative X or negative Y coordinates. This seems to break the bezier conversion and produce a garbage outline; TODO: some code to shift the paths into the positive 1st quadrant.

You need to know the decimal designation of the unicode glyph or range of glyphs you want to convert, i.e. U+05D0 is 1488, and U+05F4 is 1524.

To extract the glyphs ranging from U+05D0 to U+05F4 inclusive from your truetype font, you do the following:

	ttf2svg MiriamCLM-Book.ttf -l 1488 -h 1524 -o thing.svg	

You want a reasonable number of glyphs in your extract, so that the code can reliably determine the limb widths of the font glyphs if you are trying to make a centreline defined glyph, meaning you do not need to figure out by trial and error plus specify it manually.

If you then view thing.svg in a text editor, you'll see a set of paths defined, such as

d="M435 415V80Q435 70 445 70H483Q497 70 503 64T509 44V26Q509 12 503 6T483 0H76Q62 0 56 6T50 26V44Q50 58 56 64T76 70H355Q365 70 365 80V425Q36 5 484 342 507T260 530H98Q84 530 78 536T72 556V574Q72 588 78 594T98 600H250Q351 600 393 558T435 415Z"

Each of these paths defines the outline of the character, and sometimes there will be additional paths for the inside edges of loops in a glyph, i.e. the figure 8 will have three paths. The outer path(s) is clockwise, the inner path(s) counterclockwise.

The pathway diverges here. For centreline defined font glyph creation, things get a bit hairy:

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

-ep Y  export polygons only (experimental), with Y offset (typically value 6333 to 8500)

For lihata (.lht) polygonal glyph generation, the process is much simpler:

For example:

to generate a lihata (.lht) font file for use in pcb-rnd

java OutlineFont2centrelineFont -ep 8500 -s KLINGON.svg -f 1 -l 76

which will create a file KLINGON.svgFont.lht containing 76 glyphs drawn with polygons defined by the trutype font paths. Until additional code is written, minor hand editing is required to merge polygonal paths (taking care to make them non intersecting), and name the glyphs with standard ASCII chars.

The offset of 8500 can be experimented with to ensure all exported glyphs in the font have positive y values.

If the exported polygons don;t render properly in pcb-rnd, it may be due to glyphs being defined with paths that lie outside of the 1st quadrant with psotive X and poisitive y coordinates. If you see negatiive values for some of the polygon coordinates in the font, this would be confirmation. Once some path translation code is written, this will not be an issue.

An example for centreline creation:

This command is used to generate a footprint from a trutype glyph to allow further editing, and then ultimately, with further conversion steps, a centreline defined glyph to be generated...

java OutlineFont2centrelineFont -se -ssv -sf -ss -op -cl -s thing1.svg -g 1

Once the output file footprint has been generated, the easiest way from there is to view it in PCB, i.e. run

	pcb myNewThing.fp &

or

	pcb-rnd myNewThing.fp &

and then open the same footprint in a text editor. Changes made and saved to the footprint will show up in PCB when it refreshes on noticing a change in the footprint file.

The footprints sections are commented, showing which elements are for simple drawn lines, and which elements represent a quadratic bezier.

Until the code is refined enough to automatically censor overlaid paths from opposite sides of a limb, this has to be done manually, by commenting out sections in the footprint.

Alternatively, you may opt for a more complex footprint, comprised of two overlapping paths, but this is less efficient than a centreline defined glyph.

If further editing is needed, in gEDA PCB or pcb-rnd, the element can be selected, then "cut to buffer", then "Break buffer into pieces", after which the main task of deleting un-necessary or artefactual line elements, and simplifying joins between lines, can be done.

Once done, the elements are selected, "cut to buffer", "convert to element", and then saved.

This becomes the set of line descriptions for the centreline defined glyph.

It is at this point that the footprint needs to be converted to a gEDA PCB compatible font symbol.....

To be continued...

Already converted fonts can be found at EDAkrill, the new EDA toolchain agnostic repository for footprints, symbols, fonts and anything else EDA related:

http://repo.hu/projects/edakrill/

If you do a parametric search and look for fonts, you can see the Klingon, osifont and previously converted Hershey style fonts.

TODO:

centreline code: 

- sort out the occasional error with bezier segment stitching, which manifests as a join between bezier segments jumping to the origin (0,0).

- automatic censoring of overlaid paths from each side of the limb

- distinguishing between, and processing accordingly, glyphs with and without internal contours

polygonal glyph export to pcb-rnd code:

- coping with converted truetype fonts which have paths extending into negative X or negative Y coordinates. This is not common but will break the bezier conversion and produce garbage outlines; some code is needed to shift the paths into the positive 1st quadrant.

- automatic merging of inner and outer paths which are currently exported as separate polygons.
