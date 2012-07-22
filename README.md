LDA_for_FITS
============

run latent Dirichlet allocation (using Gibbs sampling) on fitsfiles

*******************************************************************************
                                                                             
 Run LDA on fits files                                                       
                                                                             
 By Anna Friedlander & Dr Marcus Frean                                       
                                                                             
 email: anna.fr@gmail.com                                                    
                                                                             
*******************************************************************************

First, process the fits file with readfits.py

    usage: python readfits.py fitsfile outstem

This converts the fitsfile image data to a space delimited .txt file with one 
comment line at the top of the file: "# filename x-dim y-dim"


Make the bin borders with fits-to-binborders.py

    usage: python bin borders fitsfile.fits binning_strategy num_bins binborders_filename

    possible strategies: occupancy, width, expwidth

This creates a file with the specified number of bin borders, using the 
specified strategy (e.g. 100 equal width bins) for a histogram of the pixel 
values. Bins are the "words" in the "vocabulary".


Then, make the CDW file with MakeDocs.java

    usage: java MakeDocs inputfile.txt window outstem binsfile
    
    inputfile.txt is output from readfits.py
    binsfile is output from fits-to-binborders.py
    window is the window size

This converts the image data to a CDW matrix (docs*words), a space delimited
.txt file with one comment line at the top of the file ("# filename x-dim 
y-dim"), word names as column headers, and document names as row headers.
Documents are square windows with dimension specified (the image is the corpus).
FIXME: window must be a divisor of the image's x and y dimensions.


Now you can run Gibbs sampling using Gibbs.java

    usage: java Gibbs inputfile #topics #iterations

Collapsed Gibbs sampling to infer topic allocations per word, per doc; from which topic
proportions and word-topic distributions can be derived. Outputs CDWT, CDT, and
CWT files every (#iterations/10)th iteration (file format as per CDW).
FIXME: optimise alpha and beta vectors (instead of using heuristic values); 
implement "perplexity"


To make figures of the output, use make_figs.py

    usage: python make_figs.py fname1_CDT.txt ... fnameN_CWT.txt fnameN_CDT.txt

The figures are the vocab distribution for each topic, and the overall topic 
proportions; per pair of CDT and CWT input files (for iteration 0, 10, ... , N 
of the Gibbs sampling).


To highlight the windows with the least "background" (where background is 
assumed to be the most dominant topic) for a particular iteration, use
highlight_windows.py

    usage: python highlight_windows.py fitsfile cdt-file winsize im-contrast win-darkness outfile

    im-contrast is a float value >=1.0
    a smaller im-contrast increases contrast in the image
    try starting with 0.01 and decreasing to see more sources
    win-darkness is an integer >= 1
    a larger win-darkness value increases the darkness of the red window 
    borders in the image

This is output as a fits file (primary image).


NOTE: Gibbs.java and make_figs.py can be used for other types of data (eg a document 
corpus), so long as the CDW matrix is input in the required format.