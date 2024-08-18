import java.lang.Math;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.Integer;

// Hello Welcome to the pokestat optimization thing
//First off, before downloading and running random code on your computer, you should read it!


public class PokeOp {

    public List<List<String>> csvreader(String filename){

        List<List<String>> stock = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                stock.add(Arrays.asList(values));
    
            }
    
            br.close();
        }
        catch (IOException e) {
      e.printStackTrace();
    }
            return stock;
    }
    public float calcstat( int v, int iv, int ev){
        return (2*v+iv+ev/4)/2 + 5;

    }
    public float calchp( int v, int iv, int ev){
        return (2*v+iv+ev/4)/2 + 60;


    }

    public float calcdamagep( int av, int aiv, int aev, int hv, int hiv, int hev){
        return (((22*90*(140/calcstat(av,aiv,aev)))/50) +2)/calchp(hv,hiv,hev);
    }
    public double dcalcdamagep( int av, int aiv, int aev, int hv, int hiv, int hev){
        return (((22*90*(140/calcstat(av,aiv,aev)))/50) +2)/calchp(hv,hiv,hev);
    }

    public int[] optimize(int av, int aiv, int aev, int hv, int hiv, int hev, int bound){
        int x =bound/2;
        int jump = bound/2;
    
        for(int i =0; i<12; i++){
            float t = 10000;
            int s = 0;

            


            float[] lst = {calcdamagep(av, aiv, x, hv,  hiv,  bound-x),calcdamagep(av, aiv, x+jump/2, hv,  hiv,  bound-(x+jump/2)),calcdamagep(av, aiv, x-jump/2, hv, hiv, bound-(x-jump/2))};

            for(int j =0; j<3;j++){
                if(t>lst[j]){
                    t = lst[j];
                    s = j;
                    
                }
            }
            if(s==0){
                break;
            }
            if(s == 1){
                x = x + jump/2;
                
            }
            else{
                x = x - jump/2;
            }
            jump = jump/2;
            



        
        }
            int[] rv = {x,bound};
        return rv;

    }
    public int op2(int av, int aiv, int aev, int hv, int hiv, int hev, int bound){
        float b = 10000;
        int r = 0;
        for(int i=0;i<bound;i++){
            float x = calcdamagep(av, aiv, i, hv,  hiv,  bound-i);
            if(x<b){
                b = x;
                r = i;

            }



        }
        return r;

    }



    public int[] optimize3(int dv, int spdv, int hv, int bound){
        //take directional derivative
        //these are damage for def, spdef, and health
        //just going to assume perfect IV for sake of speed programming
        //start all of them at 0
        int dev = 0;
        int spdev = 0;
        int hev =0;
        int stepsize=12;
        double x=0;
        int index = 1;
        int[] optimizedstats = {dev, spdev,hev};

        while(spdev + hev+ dev<bound){
            
            //This derivative is backwards because calcdamagep(x) > calcdamagep(x+h) but we want to move in the direction
            // that decreases damage, so we want our atk change to be multiplied by a positive value
            double dfddev = (dcalcdamagep(dv, 31, dev, hv,  31,  hev) -dcalcdamagep(dv, 31, dev+stepsize, hv,  31,  hev));
            double dfdspdev = (dcalcdamagep(spdv, 31, spdev, hv,  31,  hev) -dcalcdamagep(spdv, 31, spdev+stepsize, hv,  31,  hev));
            double dfdhev = (dcalcdamagep(dv, 31, dev, hv,  31,  hev) -dcalcdamagep(dv, 31, dev, hv,  31,  hev+stepsize)+dcalcdamagep(dv, 31, spdev, hv,  31,  hev) -dcalcdamagep(dv, 31, spdev, hv,  31,  hev+stepsize));
            


            // this gets very close to an optimized spread, but to fix it
            // i need to move in the direction of most change first
            double[] dflist = {dfddev,dfdspdev,dfdhev};


            x=-10.0000;
            index =1;
            for(int i =0; i<3;i++){
                if(optimizedstats[i]>=252){
                    continue;
                }

                

                if(dflist[i]> x){
                    x = dflist[i];

                    index = i;
                }
            }
            optimizedstats[index] = optimizedstats[index] +4;
            dev = optimizedstats[0];
            spdev = optimizedstats[1];
            hev = optimizedstats[2];
            


            

        }
        return optimizedstats;
    }
    public int[] optimizepoke3(String pokemon , int bound){
        List<List<String>> pokecsv = csvreader("pokedex.csv");
        int d = 0;
        int spd =0;
        int h=0;

        for(int i=0; i<pokecsv.size(); i++){
            

            if(pokemon.equals(pokecsv.get(i).get(0))){

                d = Integer.valueOf(pokecsv.get(i).get(5));
                spd = Integer.valueOf(pokecsv.get(i).get(7));
                h = Integer.valueOf(pokecsv.get(i).get(3));
                break;
            }



        }

        int[] optimizedstats = optimize3(d,spd,h,bound);


        return optimizedstats;

    }
    


    public static void main(String[] args){
        PokeOp poke = new PokeOp();

        String[] stats = {"DEF", "SPDEF","HP"} ;

        //Here we can change string pkm to any string that is the name of a pokemon
        //This looks them up in the pokedex.csv file
        // you can also specify how many EV's you have left to distribute in AEV, Have fun!
        //compile with javac PokeOp.java
        //then run with java PokeOp.java
        //This will print out all 3 stats one on each line in terminal
        
        String pkm = "Applin";
        int AEV = 252 ;
        System.out.println(pkm);
        for(int i=0;i<3;i++){
            System.out.println(stats[i]+ " " + poke.optimizepoke3(pkm, AEV)[i]);

        }


    }



}
