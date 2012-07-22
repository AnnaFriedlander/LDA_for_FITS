import numpy as np
import pyfits
import matplotlib.pyplot as plt
import sys

"""
readfits.py converts fitsfile image data to a space delimited .txt file with
one comment line at the top of the file: "# filename x-dim y-dim"

    usage: python readfits.py fitsfile outstem

By Anna Friedlander
email anna.fr@gmail.com
"""





"""read in image"""
def read_image(fitsfile):

    #read in image
    print 'reading image'
    hdulist = pyfits.open(fitsfile,memmap=True)
    z = hdulist[0].data
    hdulist.close()
    print 'done reading image'

    #get rid of any empty outer arrays
    while z.shape[0] == 1:
        z = z[0]
    #TODO: deal with nans

    return z


"""print fits matrix"""
def print_fits(out, z):
    print 'printing fits to text file'
    out = out + ".txt"
    # Write the array to file
    with file(out, 'w') as outfile:
        #header
        outfile.write('# %s %s %s\n' % (out, z.shape[0], z.shape[1]))
        #data
        np.savetxt(outfile, z, fmt='%.20f')
    print 'done'



if len(sys.argv) == 2:
    fitsfile = sys.argv[1]
    outfile  = fitsfile.split('.')[0]

    z = read_image(fitsfile)
    print_fits(outfile,z) 
 

else:
    sys.exit('usage: python %s fitsfile' % (sys.argv[0]))
