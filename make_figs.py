import numpy as np
import pylab as pl
import matplotlib.mlab as mlab
import matplotlib.pyplot as plt
import scipy as sp
import sys

"""
By Anna Friedlander
email anna.fr@gmail.com

Takes output of Gibbs.java and makes histogram of word frequencies/topic
and pie-chart of topic proportions.

usage: python %s fname1_CWT.txt fname1_CDT.txt ... fnameN_CWT.txt fnameN_CDT.txt

CWT file should be a space delimited list of topics per word in the vocab 
(one word per line), with comment lines marked with a #

The CWT filename must have the format name_CWT.txt 
eg: final_CWT.txt or iter20_CWT.txt

CDT file should be a space delimited list of topics per document
(one document per line), with comment lines marked with a #

The CDT filename must have the format name_CDT.txt 
eg: final_CDT.txt or iter20_CDT.txt


"""


def makeplots(CWT,CDT,title):
    T = CWT.shape[1]
    V = CWT.shape[0]
    phis = np.zeros((T,V))
    vocab = np.arange(V)
    LABELS = []
   
    for t in range(T):
        LABELS.append('topic%s' % t)
        denom = np.sum(CWT[:,t])
        for v in range(V):
            phis[t,v] = CWT[v,t]/denom
        plt.clf()
        width = 0.8 
        plt.bar(vocab,phis[t],width)
        plt.title('Topic %s, %s' % (t, title))
        plt.xlabel('Words')
        plt.xticks(np.arange(0+width/2.,V+1+width/2.,10), np.arange(0,V,V/10), size='small')
        plt.yticks(np.arange(0.0,1.05,0.1))
        plt.savefig('topic_%s_%s' % (t, title))
    topicprops = np.sum(CDT,axis=0)/np.sum(CDT)
    plt.clf()
    plt.pie(topicprops,labels=LABELS)
    plt.title('Topic proportions, %s' % title)
    plt.savefig('topic_props-%s' % title)


if len(sys.argv) >= 3:
    for i in range(1,(len(sys.argv)),2): 
        #CWT
        CWT = np.genfromtxt(sys.argv[i], comments='#')
        #get rid of row and col titles
        CWT = sp.delete(CWT,0,0)
        CWT = sp.delete(CWT,0,1)
        #CDT
        CDT = np.genfromtxt(sys.argv[i+1], comments='#')
        #get rid of row and col titles
        CDT = sp.delete(CDT,0,0)
        CDT = sp.delete(CDT,0,1)
        title = sys.argv[i].split('_')[0]
        #make figures
        makeplots(CWT,CDT,title)
else:
    sys.exit('usage: python %s fname1_CWT.txt fname1_CDT.txt ... fnameN_CWT.txt fnameN_CDT.txt'
             % (sys.argv[0]))



