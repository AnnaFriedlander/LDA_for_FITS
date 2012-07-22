import numpy as np
import scipy as sp
import pylab as pl
import pyfits
import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle
import sys

"""
To highlight the windows with the least "background" (where background is 
assumed to be the most dominant topic) for a particular iteration

    usage: python highlight_windows.py fitsfile cdt-file winsize im-contrast win-darkness outfile

    im-contrast is a float value >=1.0
    a smaller im-contrast increases contrast in the image
    try starting with 0.01 and decreasing to see more sources
    win-darkness is an integer >= 1
    a larger win-darkness value increases the darkness of the red window 
    borders in the image

This is output as a fits file.


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

""" get CDT array, and return array showing 
    proportion of background in each document """
def get_thetas(cdt):
    #get CDT array
    CDT = np.genfromtxt(cdt, comments='#')
    CDT = sp.delete(CDT,0,0)
    CDT = sp.delete(CDT,0,1)

    #get topic proportions
    topicprops = np.sum(CDT,axis=0)/np.sum(CDT)
    #get background (most common topic)
    background = np.argmax(topicprops)

    D = CDT.shape[0]
    T = CDT.shape[1]

    thetas = np.zeros((D,T))

    for t in range(T):
        thetas[:,t] = CDT[:,t]/np.sum(CDT,axis=1)

    #get proportion of background topic in each doc
    bg = thetas[:,background]

    return bg


"""highlight the docs with least background """
def make_png(Z,BG,winsize,clim,A,outfile):
    
    #plot base image
    plt.clf()
    p=plt.imshow(Z,interpolation='nearest',cmap='gray')
    p.axes.get_xaxis().set_visible(False)
    p.axes.get_yaxis().set_visible(False)
    plt.clim(0,clim)
    #plot least background windows
    count = 0
    win = winsize
    for x in range(0,Z.shape[0],win):
        for y in range(0,Z.shape[1],win):
            rect = Rectangle((y,x),win,win, edgecolor='red', facecolor='none', alpha=A*(1-BG[count]))
            pl.gca().add_patch(rect)
            count += 1


    #save image
    plt.savefig(outfile)
    plt.show()


if len(sys.argv) == 7:
    fitsfile = sys.argv[1]
    cdtfile  = sys.argv[2]
    window   = int(sys.argv[3])
    imclim   = sys.argv[4]
    walpha   = int(sys.argv[5])
    outfile  = sys.argv[6]

    z = read_image(fitsfile)
    bg = get_thetas(cdtfile)
   
    make_png(z,bg,window,imclim,walpha,outfile)

else:
    print '\nusage: python %s fitsfile cdt-file winsize im-contrast win-darkness outfile\n' % (sys.argv[0])
    print 'im-contrast is a float value >=1.0'
    print 'a smaller im-contrast increases contrast in the image'
    print 'try starting with 0.01 and decreasing to see more sources\n'
    print 'win-darkness is an integer >= 1'
    print 'a larger win-darkness value increases the darkness of the red window borders in the image\n'
    sys.exit(1)
  



