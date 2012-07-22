"""
Read in a FITS file, and binning strategy to use.
Writes a plain text file of the bin borders.
By Dr Marcus Frean
"""

import numpy as np
import numpy.random as rng
import pyfits, sys
import pylab as pl
import matplotlib.mlab as mlab
import matplotlib.pyplot as plt
import scipy as sp

if len(sys.argv) == 5:
    fitsfile = sys.argv[1]
    strategy = sys.argv[2]
    num_bins = int(sys.argv[3])
    outfile  = sys.argv[4]
else:
    print 'possible strategies: occupancy, width, expwidth...'
    sys.exit('usage: python %s fitsfile.fits  binning_strategy  num_bins  binborders_filename' % (sys.argv[0]))
    
#read in image
hdulist = pyfits.open(fitsfile,memmap=True)
raw_image = hdulist[0].data
hdulist.close()
image_dims = raw_image.shape
z = np.ravel(raw_image.copy())
while z.shape[0] == 1: z = z[0]
z.sort()
top, bottom = z[-1], z[0]
print 'Using bin strategy %s.' % (strategy)
print 'top ', top, '   bottom ',bottom
mybins = []

if strategy == 'occupancy': # equal usage
    print 'This option makes bins of approx equal usage, overall.'
    step = len(z)/num_bins
    for i in range(0,len(z)-step+1,step):
        mybins.append(z[i])
    mybins.append(z[-1]) # the last one.

elif strategy == 'width': # equally sized bins
    print 'This option is simplest: bins of equal sizes.'
    step = (top-bottom)/(num_bins+0.1)
    mybins = [bottom + x*step  for x in range(0, num_bins)]
    mybins.append(z[-1]) # the last one.

elif strategy == 'expwidth': # bins of size that increase exponentially
    print 'This option makes bins of exponentially increasing widths'
    L = np.log(top-bottom)
    step = L/(num_bins+0.1)
    mylogbins = np.array([x*step  for x in range(0, num_bins)])
    mybins = bottom + (np.exp(mylogbins)-1.0)
    mybins = list(mybins)
    mybins.append(z[-1]) # the last one.

else: sys.exit('no such strategy')

# move the top and bottom borders to be well beyond the current data
safety_gap =  (top-bottom)/20.0
mybins[-1] += safety_gap
mybins[0]  -= safety_gap

# quick checks
if len(mybins) != (num_bins +1):
    sys.exit('oops: len(mybins) != (num_bins+1), but they should match.')

# write them out
fp = open(outfile, 'w')
for b in mybins:
    fp.write('%.20f\n' % (b))
fp.close()
print 'Wrote bin borders to %s.' % (outfile)

#make histogram of overall image, P
plt.gray()
n_all, bins_all, patches = plt.hist(np.ravel(z), bins=mybins, alpha=0.75)
plt.title('histogram using those bins')
plt.draw()
plt.savefig(outfile + '_test_histogram')


