/******************************************************************************
Collapsed Gibbs sampling for latent Dirchlet allocation on images.

Infers topic allocations per word, per doc; from which topic proportions and 
word-topic distributions can be derived. Outputs CDWT, CDT, and CWT files every 
(#iterations/10)th iteration (file format as per CDW).

    usage: java Gibbs inputfile #topics #iterations

NOTE: can be used for other types of data (eg a document 
corpus), so long as the CDW matrix is input in the required format.

TODO: optimise alpha and beta vectors (instead of using lame heuristic values),
implement "perplexity", let either CWD or CDWT be read in


By Anna Friedlander
email anna.fr@gmail.com

******************************************************************************/
import java.util.*;
import java.io.*;

public class Gibbs{
//number of topics, words in vocab, documents
private int D;
private int V;
private int T;
//alpha and beta hyperparameters
private double alpha = 0.1;
private double beta = 0.01;
//matrices
private int    [][][] CDWT;
private double [][]   CWT;
private double [][]   CDT;
private double []     CT;
private double []     probs;
//arrays for word and doc names
private String [] wordnames;
private String [] docnames;


/*fill matrices, run Gibbs, and print final CWT and CDT*/
public Gibbs(String fname, int numtopics, int iters){
    long start = System.currentTimeMillis();

    //get number of topics
    T=numtopics;

    //fill matrices
    matrices(fname);
    //Gibbs
    gibbs_sampling(iters, iters/10);

    long end = System.currentTimeMillis();
    print_times(start, end, "TOTAL TIME:");
}


/* make CDWT, CWT, CDT matrices and initialise D & V parameters */
public void matrices(String infile){

    System.out.println("reading infile and making matrices");
    int num,t;
    Random rdm = new Random();

    //read CDW file in
    try{
        Scanner scan = new Scanner(new File(infile));
        //parse header to get D & V
        scan.next(); scan.next();
        D = scan.nextInt();
        V = scan.nextInt();
        scan.nextLine();
    
        CDWT = new int   [D][V][T];
        CWT  = new double[V][T];
        CDT  = new double[D][T];
        CT   = new double[T];
        probs= new double[T];
        
        wordnames = new String[V];
        docnames  = new String[D];

        //get wordnames
        scan.next();
        for(int w=0;w<V;w++) wordnames[w]=scan.next();
        scan.nextLine();

        //fill in matrices
        for(int d=0;d<D;d++){
            docnames[d] = scan.next();
            for(int v=0;v<V;v++){
                num = scan.nextInt();

                //initialise topics randomly
                for(int i=0;i<num;i++){
                    t = rdm.nextInt(T);
                    CDWT[d][v][t] += 1;
                    CWT [v][t]    += 1;
                    CDT [d][t]    += 1;
                    CT  [t]       += 1;

                }

            }
        scan.nextLine();
        }
        scan.close();
        }
        catch(IOException e){
            System.out.println("File reading failed: "+e);
            System.exit(1);
            }

    //add alpha to CDT and beta to CWT & CT
    for(int d=0;d<D;d++){
        for(t=0;t<T;t++) CDT[d][t]+=alpha;
    }
    for(int v=0;v<V;v++){
        for(t=0;t<T;t++){
            CWT[v][t] += beta;
            CT [t]    += beta;
        }
    }

    System.out.println("reading infile and making matrices done");

}

/* Gibbs sampling */
public void gibbs_sampling(int iters, int n){

    System.out.println("starting Gibbs");
    int iter = iters;
    int dvt;
    int zi;
    double num;
    double sum;

    for(int i=0;i<iter;i++){
        if((i%10) == 0) System.out.println("iter "+i);
        if((i%n)  == 0) print(("iter"+i),CWT,CDT);
        for(int d=0;d<D;d++){
           for(int v=0;v<V;v++){
               for(int t=0;t<T;t++){
               dvt = CDWT[d][v][t];
                   for(int g=0;g<dvt;g++){
                       //decrement entry in matrices corresponding to topic
                       CDWT[d][v][t] -= 1;
                       CWT [v][t]   -= 1;
                       CDT [d][t]   -= 1;
                       CT  [t]     -= 1;
                       //calculate(zi = j) the probability of the current topic 
                       //assignment given the current distribution
                       sum = 0;
                       for(int j=0;j<T;j++){
                           num = CWT[v][j]/CT[j] * CDT[d][j];
                           probs[j] = num;
                           sum += num;
                       }
                       //normalise
                       for(int j=0;j<T;j++) probs[j] /= sum;
                       //convert to cummulative distribution function
                       for(int j=1;j<T;j++) probs[j] += probs[j-1];
                       //assign token zi a topic based on new probabilities (drawn 
                       //from a categorical distribution with parameters in probs)
                       Random rand = new Random();
                       double val = rand.nextDouble();
                       zi = binsearch(probs,val);
                       //increment entries in matrices corresponding to new topic
                       CDWT[d][v][zi] += 1;
                       CWT [v][zi]   += 1;
                       CDT [d][zi]   += 1;
                       CT  [zi]     += 1;
                   }   
               }
           }
        }
    }
    System.out.println("Gibbs done");

    System.out.println("printing final CWT and CDT matrices");
    print("final",CWT,CDT);
    System.out.println("done printing matrices");

}


/*print CWT and CDT matrices
  TODO: address redundancy */
public void print(String label, double[][]cwt, double[][]cdt){
    try{

        //print CDWT
        String OUT = (label + "_CDWT.txt");
        PrintStream out = new PrintStream(new File(OUT));
        out.printf("#%s %s %s %s\n",D,V,T,label);
        for(int d=0;d<D;d++){
            out.printf("# DOC #%d: %s \ntopics: ",d,docnames[d]);
            for(int t=0;t<T;t++) out.printf("%s ",t);
            out.printf("\n");
            for(int v=0;v<V;v++){
                out.printf("%s ",wordnames[v]);
                for(int t=0;t<T;t++)
                    out.printf("%.3f ",cwt[v][t]);
                out.printf("\n");
            }
        }
        out.close();

        //print CWT
        OUT = (label + "_CWT.txt");
        out = new PrintStream(new File(OUT));
        out.printf("#%s %s %s\ntopics: ",V,T,label);
        for(int t=0;t<T;t++) out.printf("%s ",t);
        out.printf("\n");
        for(int v=0;v<V;v++){
            out.printf("%s ",wordnames[v]);
            for(int t=0;t<T;t++)
                out.printf("%.3f ",cwt[v][t]);
            out.printf("\n");
        }
        out.close();

        //print CDT
        OUT = (label + "_CDT.txt");
        out = new PrintStream(new File(OUT));
        out.printf("#%s %s %s\ntopics: ",D,T,label);
        for(int t=0;t<T;t++) out.printf("%s ",t);
        out.printf("\n");
        for(int d=0;d<D;d++){
            out.printf("%s ",docnames[d]);
            for(int t=0;t<T;t++)
                out.printf("%.3f ",cdt[d][t]);
            out.printf("\n");
        }
        out.close();
    }catch(IOException e){System.out.println("File writing failed: "+e);}
}


/*binary search*/
public int binsearch(double[]probs, double val){
    int lo = 0;
    int hi = probs.length - 1;
    int mid;
    while(lo <= hi){
        mid = (lo+hi)/2;
        if(probs[mid] > val)      hi = mid-1;
        else if(probs[mid] < val) lo = mid+1;
        else                      return mid;
    }
    return lo;
}



/*helper method to time program*/
public void print_times(long start, long stop, String message){
    long time = stop-start;
    System.out.println(message);
    System.out.println("mins: "+time/(60*1000F));
    System.out.println("secs: "+time/1000F);
}


/*input format: java Gibbs inputfile outstem iterations*/
public static void main(String args[]){
    String input,message="usage: java Gibbs CWD-inputfile #topics #iterations";
    int topics=0,iters=0;

    if(args.length != 3){System.out.println(message);System.exit(1);}

    try{ 
        topics = Integer.parseInt(args[1]);
    }catch(NumberFormatException e){System.out.println(message);System.exit(1);}

    try{ 
        iters = Integer.parseInt(args[2]);
    }catch(NumberFormatException e){System.out.println(message);System.exit(1);}

    input = args[0];
    new Gibbs(input,topics,iters);
}

}
 

