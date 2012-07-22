/******************************************************************************

Transforms FITS files in txt format (readfits.py output) into CDW matrix

    usage: java MakeDocs inputfile.txt window outstem binsfile
    
    inputfile.txt is output from readfits.py
    binsfile is output from fits-to-binborders.py
    window is the window size

Converts image data to a CDW matrix (docs*words), a space delimited
.txt file with one comment line at the top of the file ("# filename x-dim 
y-dim"), word names as column headers, and document names as row headers.
Documents are square windows with dimension specified (the image is the corpus).

FIXME: window must be a divisor of the image's x and y dimensions.


By Anna Friedlander
email anna.fr@gmail.com

******************************************************************************/
import java.util.*;
import java.io.*;

public class MakeDocs{
//matrices
private double [][] z;
private ArrayList<Double> BINS;
private int [][] CDW;
//parameters
int D;
int V;
int X;
int Y;
int win;
int numbins;

public MakeDocs(String infile, int w, String out, String bins){
    long start = System.currentTimeMillis();

    win = w;

    z = make_z(infile);
    BINS = make_bins(bins);
    V=BINS.size()-1;
    CDW = make_CDW(z,BINS);
    print_CDW(infile,CDW,BINS,out);

    long end = System.currentTimeMillis();
    print_times(start, end, "TOTAL TIME:");
}

/* make data array */
public double[][] make_z(String infile){
    System.out.println("reading infile");
    //data matrix
    double[][] z = new double[0][0];

    //read fitsfile.txt in
    try{
        Scanner scan = new Scanner(new File(infile));
        //parse header to get dimensions
        scan.next();
        scan.next();
        X = scan.nextInt();
        Y = scan.nextInt();
        scan.nextLine();
    
        z = new double[X][Y];

        //fill in data array
        for(int x=0;x<X;x++){
            for(int y=0;y<Y;y++) 
                z[x][y] = scan.nextDouble();
            scan.nextLine();
        }
        scan.close();
        }
        catch(IOException e){
            System.out.println(infile+" file reading failed: "+e);
            System.exit(1);
            }
    System.out.println("reading infile done");

    return z;   
}

/*make binsfile*/
public ArrayList make_bins(String binsf){
    System.out.println("reading binsfile");
    ArrayList<Double>bins = new ArrayList<Double>();

    //read bins
    try{
        Scanner scan = new Scanner(new File(binsf));
        while(scan.hasNext()){
            bins.add(scan.nextDouble());
            scan.nextLine();
        }
        scan.close();
    }
    catch(IOException e){
        System.out.println(binsf+" binsfile reading failed: "+e);
        System.exit(1);
        }

    System.out.println("binsfile reading done");
    return bins;
}


/*make CDW*/
public int[][] make_CDW(double[][]z, ArrayList<Double>bins){

    int[][]cdw;
    double num;

    //num documents
    D = (z.length/win)*(z[0].length/win);

    //initialise cdw
    cdw = new int[D][V];

    //get 'word counts' from 'documents' (ie bin-counts in windows)
    //TODO: fix the iteration of windows - will break if x%win!=0 and y%win!=0
    System.out.println("getting words and docs");
    int doc = 0;
    for(int x=0;x<X;x+=win){
        for(int y=0;y<Y;y+=win){

            for(int xwin=0;xwin<win;xwin++){
                for(int ywin=0;ywin<win;ywin++){
                    num = z[x+xwin][y+ywin];
                    for(int v=0;v<bins.size()-1;v++){
                        if(num>=bins.get(v) && num<bins.get(v+1)){
                            cdw[doc][v]++;
                            break;
                        } 
                    }
                }
            }

            doc += 1;
            if (doc%100 == 0) System.out.println(doc);
        }
    }

    System.out.println("got words and docs");

    return cdw;
}

/*print out CDWT*/
public void print_CDW(String infile, int[][]cdw, ArrayList<Double>bins, String outfile){

    System.out.println("printing CDW");

    try{
        outfile = (outfile + "_CDW.txt");
        PrintStream out = new PrintStream(new File(outfile));
        out.printf("# %s %s %s\nbins: ",infile,D,V);
        //print bin names
        for(int b=0;b<bins.size()-1;b++)
            out.printf("%.20f_to_%.20f ",bins.get(b),bins.get(b+1));
        out.printf("\n");
        //print data
        for(int d=0;d<D;d++){
        out.printf("doc_%s ",d);
            for(int v=0;v<V;v++){
                out.printf("%d ",cdw[d][v]);
            }
            out.printf("\n");
        }
        out.close();


    }
    catch(IOException e){System.out.println("CDW file writing failed: "+e);
                         System.exit(1);}

    System.out.println("done");

}


/*helper method to time program*/
public void print_times(long start, long stop, String message){
    long time = stop-start;
    System.out.println(message);
    System.out.println("mins: "+time/(60*1000F));
    System.out.println("secs: "+time/1000F);
}


/*input format: java MakeDocs inputfile.txt numtopics numwords window outstem numbins binsfile*/
public static void main(String args[]){
    String input;
    int win;
    String out;
    String binsfile;

    if(args.length != 4){
        System.out.printf("\nusage: java MakeDocs inputfile.txt window outstem binsfile\n");
        System.out.printf("\ninputfile.txt is output from readfits.py\n");
        System.out.printf("binsfile is output from fits-to-binborders.py\n\n");
    }

    else{
        input = args[0];
        win   = Integer.parseInt(args[1]);
        out   = args[2];
        binsfile = args[3];
        
        new MakeDocs(input, win, out, binsfile);
    }

}


}
