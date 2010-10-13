package etomica.potential;
import etomica.space.ISpace;
import etomica.space.Space;
import etomica.space3d.Space3D;
import etomica.util.ParameterBase;
import etomica.util.numerical.AkimaSpline;

/**
 * Pair potential for argon interpolated from Q-Chem results.  
 * 
 * In this class, only the pair potential is valid, not the gradients, etc.  I am unlikely to ever include those...
 *
 * @author Kate Shaul
 */
public class P2QChemInterpolated extends Potential2SoftSpherical {
    
    public P2QChemInterpolated(ISpace space) {
    	
        super(space);
   
    }

    /**
     * The energy u.
     */
    public void setDampingParams(int a1, int a2, double Rvdw, int basis, boolean fixedRvdw) {
    	this.a1 =a1;
    	this.a2 = a2;
    	this.Rvdw = Rvdw;
    	this.basis = basis;
    	this.fixedRvdw = fixedRvdw;
    	
    }
   
    
    public double u(double r2) {
    	
    	
    	double r = Math.sqrt(r2);
    	double energy=0;
    	double C6=0;
		double C8=0;
		double C10=0;
		double Rc=0;
		
    	if (r < 2.5) {
    		//System.out.println("r12 = "+r12Mag+" Angstroms, u12 set to Inf");
    		return Double.POSITIVE_INFINITY;

    		
    	} else {
    		
    		double[] u12NoDisp;
    		double[] C6s; double[] C8s; double[] C10s; double[] Rcs;
    		
    		double[] rs = new double[] {2.00 , 2.10 , 2.20 , 2.30 , 2.40 , 2.50 , 2.60 , 2.70 , 2.80 , 2.90 , 3.00 , 3.10 , 3.20 , 3.30 , 3.40 , 3.50 , 3.60 , 3.65 , 3.66 , 3.67 , 3.68 , 3.69 , 3.70 , 3.71 , 3.72 , 3.73 , 3.74 , 3.75 , 3.76 , 3.77 , 3.78 , 3.79 , 3.80 , 3.81 , 3.82 , 3.83 , 3.84 , 3.85 , 3.86 , 3.87 , 3.88 , 3.89 , 3.90 , 4.00 , 4.10 , 4.20 , 4.30 , 4.40 , 4.50 , 4.60 , 4.70 , 4.80 , 4.90 , 5.00 , 5.10 , 5.20 , 5.30 , 5.40 , 5.50 , 5.60 , 5.70 , 5.80 , 5.90 , 6.00 , 6.10 , 6.20 , 6.30 , 6.40 , 6.50 , 6.60 , 6.70 , 6.80 , 6.90 , 7.00 , 7.10 , 7.20 , 7.30 , 7.40 , 7.50 , 7.60 , 7.70 , 7.80 , 7.90 , 8.00 , 8.10 , 8.20 , 8.30 , 8.40 , 8.50 , 8.60 , 8.70 , 8.80 , 8.90 , 9.00 , 9.10 , 9.20 , 9.30 , 9.40 , 9.50 , 9.60 , 9.70 , 9.80 , 9.90 , 10.00 , 11.00 , 12.00 , 13.00 , 14.00 , 15.00 , 16.00 , 17.00 , 18.00 , 19.00 , 20.00 , 21.00};

    		if (basis == 4) {
    			u12NoDisp = new double[] {1.673022554998624e-01 , 1.203211970998836e-01 , 8.590222060001906e-02 , 6.089571090001300e-02 , 4.286329229989860e-02 , 2.995013860004292e-02 , 2.076466869993965e-02 , 1.427452699999776e-02 , 9.720771599859290e-03 , 6.549592000055782e-03 , 4.359383199926015e-03 , 2.860377300066830e-03 , 1.844781500039971e-03 , 1.164998800049943e-03 , 7.159238998610817e-04 , 4.231241998695623e-04 , 2.366081000673148e-04 , 1.710978999653889e-04 , 1.598978999481915e-04 , 1.492613000664278e-04 , 1.391517998854397e-04 , 1.295321999350563e-04 , 1.203669999085832e-04 , 1.116252999509015e-04 , 1.032815998769365e-04 , 9.531859996059211e-05 , 8.772479986873805e-05 , 8.049269990806351e-05 , 7.361810003203573e-05 , 6.709779995617282e-05 , 6.092869989515748e-05 , 5.510579990186670e-05 , 4.962129992236441e-05 , 4.446269986146945e-05 , 3.961430002163979e-05 , 3.505530003167223e-05 , 3.076389998568629e-05 , 2.671690003808180e-05 , 2.289290000589972e-05 , 1.927449989125307e-05 , 1.584889992045646e-05 , 1.260699991689762e-05 , 9.541799954604357e-06 , -1.220019998982025e-05 , -2.180310002586339e-05 , -2.474470011293306e-05 , -2.360050007155223e-05 , -2.082519995383336e-05 , -1.711250001790177e-05 , -1.371849998577090e-05 , -1.040739994095929e-05 , -7.919200015749084e-06 , -5.721300112782046e-06 , -4.200900093564996e-06 , -3.019900077561033e-06 , -2.125600076396950e-06 , -1.595599997017416e-06 , -1.103999920815113e-06 , -8.417000572080724e-07 , -6.729999313392909e-07 , -4.882999746769201e-07 , -4.454000190889928e-07 , -3.784000455198111e-07 , -3.177999587933300e-07 , -3.264001406932948e-07 , -2.977001258841483e-07 , -2.757001311692875e-07 , -2.942999799415702e-07 , -2.852000307029812e-07 , -2.547999429225456e-07 , -2.689000666578067e-07 , -2.772999323497061e-07 , -2.371000391576672e-07 , -2.254000719403848e-07 , -2.500000846339390e-07 , -2.282999957969878e-07 , -1.851999513746705e-07 , -1.941000391525449e-07 , -2.143001438525971e-07 , -1.805001375032589e-07 , -1.446999249310466e-07 , -1.585999598319177e-07 , -1.798000539565692e-07 , -1.503001385572134e-07 , -1.130999862652970e-07 , -1.175999386759941e-07 , -1.463999979023356e-07 , -1.348000751022482e-07 , -9.309997039963491e-08 , -7.669996193726547e-08 , -1.010000687529100e-07 , -1.201999566546874e-07 , -9.320001481682993e-08 , -5.500010047398973e-08 , -4.999992597731762e-08 , -7.860012374294456e-08 , -9.680002222012263e-08 , -7.070002538966946e-08 , -3.269997250754386e-08 , -2.520005182304885e-08 , -5.100014277559239e-08 , -7.840003490855452e-08 , -6.789991857658606e-08 , -2.899992068705615e-08 , -5.400011104939040e-08 , 3.859986463794485e-08 , 1.850003172876313e-08 , 3.399918568902649e-09 , 7.399989954137709e-08 , 7.409998943330720e-07 , 7.275000371009810e-07 , 4.947999059368158e-07 , 3.302000095573021e-07 , 2.240999492642004e-07 , 1.547000465507153e-07};
    			C6s = new double[] {5.320000e+01 , 5.334900e+01 , 5.368400e+01 , 5.409800e+01 , 5.453500e+01 , 5.496100e+01 , 5.536600e+01 , 5.573900e+01 , 5.607500e+01 , 5.637500e+01 , 5.663700e+01 , 5.686400e+01 , 5.785400e+01 , 5.722100e+01 , 5.735700e+01 , 5.746800e+01 , 5.755900e+01 , 5.759700e+01 , 5.760400e+01 , 5.761100e+01 , 5.761800e+01 , 5.762400e+01 , 5.763100e+01 , 5.763700e+01 , 5.764300e+01 , 5.765000e+01 , 5.765500e+01 , 5.766100e+01 , 5.766700e+01 , 5.767200e+01 , 5.767800e+01 , 5.768300e+01 , 5.768800e+01 , 5.769300e+01 , 5.769800e+01 , 5.770300e+01 , 5.770800e+01 , 5.771200e+01 , 5.771700e+01 , 5.772100e+01 , 5.772500e+01 , 5.772900e+01 , 5.773300e+01 , 5.776800e+01 , 5.779500e+01 , 5.781500e+01 , 5.782900e+01 , 5.783800e+01 , 5.784400e+01 , 5.784700e+01 , 5.784800e+01 , 5.784600e+01 , 5.784400e+01 , 5.784000e+01 , 5.783600e+01 , 5.783200e+01 , 5.782700e+01 , 5.782200e+01 , 5.781800e+01 , 5.781500e+01 , 5.781100e+01 , 5.780800e+01 , 5.780500e+01 , 5.780300e+01 , 5.780100e+01 , 5.780000e+01 , 5.779800e+01 , 5.779700e+01 , 5.779600e+01 , 5.779600e+01 , 5.779500e+01 , 5.779400e+01 , 5.779400e+01 , 5.779400e+01 , 5.779300e+01 , 5.779300e+01 , 5.779200e+01 , 5.779200e+01 , 5.779200e+01 , 5.779200e+01 , 5.779200e+01 , 5.779100e+01 , 5.779100e+01 , 5.779100e+01 , 5.779100e+01 , 5.779100e+01 , 5.779000e+01 , 5.779000e+01 , 5.779000e+01 , 5.779000e+01 , 5.779000e+01 , 5.779000e+01 , 5.779000e+01 , 5.779000e+01 , 5.779000e+01 , 5.778900e+01 , 5.778900e+01 , 5.778900e+01 , 5.778900e+01 , 5.778900e+01 , 5.778900e+01 , 5.778900e+01 , 5.778900e+01 , 5.778900e+01 , 5.778900e+01 , 5.778900e+01 , 5.778900e+01 , 5.778900e+01 , 5.778900e+01 , 5.778900e+01 , 5.778900e+01 , 5.778900e+01 , 5.778900e+01 , 5.778900e+01 , 5.778900e+01};
    			C8s = new double[] {1.960250e+03 , 1.944847e+03 , 1.944201e+03 , 1.950958e+03 , 1.961560e+03 , 1.973677e+03 , 1.986388e+03 , 1.998863e+03 , 2.010766e+03 , 2.021839e+03 , 2.031905e+03 , 2.040976e+03 , 2.095801e+03 , 2.056228e+03 , 2.062386e+03 , 2.067625e+03 , 2.072082e+03 , 2.074024e+03 , 2.074395e+03 , 2.074761e+03 , 2.075113e+03 , 2.075457e+03 , 2.075804e+03 , 2.076145e+03 , 2.076480e+03 , 2.076809e+03 , 2.077118e+03 , 2.077435e+03 , 2.077746e+03 , 2.078052e+03 , 2.078351e+03 , 2.078645e+03 , 2.078934e+03 , 2.079217e+03 , 2.079494e+03 , 2.079767e+03 , 2.080035e+03 , 2.080298e+03 , 2.080546e+03 , 2.080799e+03 , 2.081038e+03 , 2.081283e+03 , 2.081524e+03 , 2.083686e+03 , 2.085425e+03 , 2.086865e+03 , 2.088033e+03 , 2.088934e+03 , 2.089706e+03 , 2.090320e+03 , 2.090796e+03 , 2.091193e+03 , 2.091524e+03 , 2.091790e+03 , 2.092005e+03 , 2.092219e+03 , 2.092367e+03 , 2.092517e+03 , 2.092668e+03 , 2.092802e+03 , 2.092891e+03 , 2.093001e+03 , 2.093069e+03 , 2.093153e+03 , 2.093210e+03 , 2.093226e+03 , 2.093269e+03 , 2.093303e+03 , 2.093327e+03 , 2.093367e+03 , 2.093368e+03 , 2.093342e+03 , 2.093372e+03 , 2.093372e+03 , 2.093359e+03 , 2.093314e+03 , 2.093298e+03 , 2.093290e+03 , 2.093290e+03 , 2.093304e+03 , 2.093290e+03 , 2.093276e+03 , 2.093263e+03 , 2.093246e+03 , 2.093231e+03 , 2.093211e+03 , 2.093171e+03 , 2.093165e+03 , 2.093150e+03 , 2.093126e+03 , 2.093114e+03 , 2.093088e+03 , 2.093086e+03 , 2.093072e+03 , 2.093071e+03 , 2.093058e+03 , 2.093052e+03 , 2.093062e+03 , 2.093042e+03 , 2.093045e+03 , 2.093037e+03 , 2.093040e+03 , 2.093042e+03 , 2.093031e+03 , 2.093019e+03 , 2.093015e+03 , 2.093020e+03 , 2.093012e+03 , 2.093009e+03 , 2.092992e+03 , 2.092967e+03 , 2.092966e+03 , 2.092977e+03 , 2.092987e+03 , 2.092994e+03};
    			C10s = new double[] {7.049860e+04 , 6.858640e+04 , 6.775830e+04 , 6.746510e+04 , 6.749020e+04 , 6.767360e+04 , 6.794700e+04 , 6.825980e+04 , 6.859270e+04 , 6.892550e+04 , 6.924140e+04 , 6.954270e+04 , 7.194000e+04 , 7.009990e+04 , 7.033960e+04 , 7.054910e+04 , 7.073520e+04 , 7.081770e+04 , 7.083400e+04 , 7.085010e+04 , 7.086500e+04 , 7.087940e+04 , 7.089490e+04 , 7.091010e+04 , 7.092510e+04 , 7.093990e+04 , 7.095250e+04 , 7.096690e+04 , 7.098100e+04 , 7.099500e+04 , 7.100870e+04 , 7.102220e+04 , 7.103560e+04 , 7.104870e+04 , 7.106160e+04 , 7.107440e+04 , 7.108690e+04 , 7.109930e+04 , 7.111020e+04 , 7.112220e+04 , 7.113270e+04 , 7.114440e+04 , 7.115590e+04 , 7.126200e+04 , 7.134730e+04 , 7.142320e+04 , 7.148660e+04 , 7.153530e+04 , 7.158250e+04 , 7.162220e+04 , 7.165380e+04 , 7.168270e+04 , 7.170950e+04 , 7.173130e+04 , 7.175040e+04 , 7.177090e+04 , 7.178430e+04 , 7.179940e+04 , 7.181490e+04 , 7.182880e+04 , 7.183750e+04 , 7.184900e+04 , 7.185570e+04 , 7.186470e+04 , 7.187030e+04 , 7.187100e+04 , 7.187530e+04 , 7.187870e+04 , 7.188090e+04 , 7.188560e+04 , 7.188510e+04 , 7.188120e+04 , 7.188470e+04 , 7.188450e+04 , 7.188260e+04 , 7.187620e+04 , 7.187380e+04 , 7.187250e+04 , 7.187220e+04 , 7.187390e+04 , 7.187170e+04 , 7.186960e+04 , 7.186760e+04 , 7.186520e+04 , 7.186310e+04 , 7.186020e+04 , 7.185460e+04 , 7.185370e+04 , 7.185140e+04 , 7.184780e+04 , 7.184600e+04 , 7.184210e+04 , 7.184150e+04 , 7.183920e+04 , 7.183870e+04 , 7.183670e+04 , 7.183570e+04 , 7.183680e+04 , 7.183410e+04 , 7.183430e+04 , 7.183320e+04 , 7.183340e+04 , 7.183350e+04 , 7.183210e+04 , 7.183020e+04 , 7.182960e+04 , 7.183020e+04 , 7.182920e+04 , 7.182900e+04 , 7.182810e+04 , 7.182670e+04 , 7.182630e+04 , 7.182690e+04 , 7.182760e+04 , 7.182800e+04};
    			Rcs = new double[] {3.1920 , 3.1680 , 3.1540 , 3.1440 , 3.1380 , 3.1340 , 3.1320 , 3.1300 , 3.1300 , 3.1300 , 3.1300 , 3.1300 , 3.1420 , 3.1300 , 3.1320 , 3.1320 , 3.1340 , 3.1340 , 3.1340 , 3.1340 , 3.1340 , 3.1340 , 3.1340 , 3.1340 , 3.1340 , 3.1340 , 3.1340 , 3.1340 , 3.1340 , 3.1340 , 3.1340 , 3.1340 , 3.1340 , 3.1340 , 3.1360 , 3.1360 , 3.1360 , 3.1360 , 3.1360 , 3.1360 , 3.1360 , 3.1360 , 3.1360 , 3.1360 , 3.1360 , 3.1380 , 3.1380 , 3.1380 , 3.1380 , 3.1400 , 3.1400 , 3.1400 , 3.1400 , 3.1400 , 3.1400 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420 , 3.1420};
    		} else if (basis == 3) {
    		//aug-cc-pVTZ
    			u12NoDisp = new double[] {1.68664581000030e-01 , 1.21167818800131e-01 , 8.64574926999921e-02 , 6.12795511999593e-02 , 4.31364400001257e-02 , 3.01436422000734e-02 , 2.08963434999987e-02 , 1.43570379000266e-02 , 9.76492080008029e-03 , 6.56514940010311e-03 , 4.35491150005873e-03 , 2.84297479993256e-03 , 1.82032290013012e-03 , 1.13831919998120e-03 , 6.90603799966993e-04 , 4.01688100055253e-04 , 2.20409800022026e-04 , 1.57775300067442e-04 , 1.47141199931866e-04 , 1.37062500016327e-04 , 1.27501499946447e-04 , 1.18421200113517e-04 , 1.09787200017308e-04 , 1.01569200069207e-04 , 9.37432000682747e-05 , 8.62915001107467e-05 , 7.92032001299958e-05 , 7.24714000170934e-05 , 6.60927000808442e-05 , 6.00635000864713e-05 , 5.43794999430247e-05 , 4.90351001189993e-05 , 4.40155999967828e-05 , 3.93074001294735e-05 , 3.48928999756026e-05 , 3.07496000004903e-05 , 2.68563001100119e-05 , 2.31907999932446e-05 , 1.97321999166888e-05 , 1.64626001151191e-05 , 1.33676001041749e-05 , 1.04376999843225e-05 , 7.66569996812905e-06 , -1.17709998903592e-05 , -2.06015999992815e-05 , -2.37691999700473e-05 , -2.37592000758013e-05 , -2.23300000925519e-05 , -2.00582999241306e-05 , -1.78297000275052e-05 , -1.53347000377835e-05 , -1.32937000216771e-05 , -1.12237999019271e-05 , -9.54689994614455e-06 , -8.10879987511726e-06 , -6.82569998389226e-06 , -5.87940007790166e-06 , -5.02199986840424e-06 , -4.37259996033390e-06 , -3.84829991162405e-06 , -3.35560002895363e-06 , -2.98569989354292e-06 , -2.62520006799605e-06 , -2.29369993576256e-06 , -2.01459988602437e-06 , -1.73319995155907e-06 , -1.47579999065783e-06 , -1.27539988170611e-06 , -1.07360006040835e-06 , -8.71699967319728e-07 , -7.43199962016661e-07 , -6.29099986326764e-07 , -4.83300027553923e-07 , -3.89700062441989e-07 , -3.49300080415560e-07 , -2.69999873125926e-07 , -1.82800022230367e-07 , -1.61699972522911e-07 , -1.56700025399914e-07 , -1.00699935501325e-07 , -5.15999545314116e-08 , -6.07999481871957e-08 , -7.76999513618648e-08 , -4.38999450125266e-08 , -9.40008249017410e-09 , -2.27998953050701e-08 , -5.90000581723871e-08 , -5.20999492437113e-08 , -2.01998773263767e-08 , -1.87999376066728e-08 , -5.61999513593037e-08 , -8.35000264487462e-08 , -6.54999894322827e-08 , -4.15000158682233e-08 , -5.03000592289027e-08 , -8.73999397299485e-08 , -1.10100017991499e-07 , -9.12000359676313e-08 , -6.36998720437987e-08 , -6.37999164609937e-08 , -9.15999862627359e-08 , -1.18400066639879e-07 , -1.09999973574304e-07 , -7.63000116421608e-08 , -7.64998731028754e-08 , 4.38999450125266e-08 , 3.35001004714286e-08 , 9.29001089389203e-08 , 1.09040001916583e-06 , 1.13770011012093e-06 , 7.55499968363438e-07 , 4.94399955641711e-07 , 3.29099975715508e-07 , 2.23099959839601e-07 , -1.00044417195022e-10 };
    			C6s = new double[] {5.359100e+01 , 5.368500e+01 , 5.399100e+01 , 5.438800e+01 , 5.481600e+01 , 5.523900e+01 , 5.564100e+01 , 5.601100e+01 , 5.634400e+01 , 5.663900e+01 , 5.689700e+01 , 5.712100e+01 , 5.731200e+01 , 5.747000e+01 , 5.760500e+01 , 5.771800e+01 , 5.781000e+01 , 5.785000e+01 , 5.785700e+01 , 5.786400e+01 , 5.787100e+01 , 5.787800e+01 , 5.788500e+01 , 5.789200e+01 , 5.789800e+01 , 5.790400e+01 , 5.791100e+01 , 5.791700e+01 , 5.792200e+01 , 5.792800e+01 , 5.793400e+01 , 5.793900e+01 , 5.794500e+01 , 5.795000e+01 , 5.795500e+01 , 5.796000e+01 , 5.796400e+01 , 5.796900e+01 , 5.797300e+01 , 5.797700e+01 , 5.798200e+01 , 5.798600e+01 , 5.799000e+01 , 5.802400e+01 , 5.804900e+01 , 5.806500e+01 , 5.807500e+01 , 5.808000e+01 , 5.808100e+01 , 5.808000e+01 , 5.807700e+01 , 5.807300e+01 , 5.806900e+01 , 5.806500e+01 , 5.806200e+01 , 5.805900e+01 , 5.805600e+01 , 5.805300e+01 , 5.805100e+01 , 5.805000e+01 , 5.804900e+01 , 5.804800e+01 , 5.804700e+01 , 5.804600e+01 , 5.804600e+01 , 5.804600e+01 , 5.804500e+01 , 5.804500e+01 , 5.804500e+01 , 5.804500e+01 , 5.804500e+01 , 5.804400e+01 , 5.804400e+01 , 5.804400e+01 , 5.804400e+01 , 5.804300e+01 , 5.804300e+01 , 5.804300e+01 , 5.804300e+01 , 5.804300e+01 , 5.804200e+01 , 5.804200e+01 , 5.804200e+01 , 5.804200e+01 , 5.804100e+01 , 5.804100e+01 , 5.804000e+01 , 5.804000e+01 , 5.804000e+01 , 5.804000e+01 , 5.803900e+01 , 5.803900e+01 , 5.803900e+01 , 5.803800e+01 , 5.803800e+01 , 5.803800e+01 , 5.803800e+01 , 5.803800e+01 , 5.803800e+01 , 5.803800e+01 , 5.803800e+01 , 5.803800e+01 , 5.803800e+01 , 5.803800e+01 , 5.803800e+01 , 5.803800e+01 , 5.803800e+01 , 5.803800e+01 , 5.803700e+01 , 5.803700e+01 , 5.803700e+01 , 5.803700e+01 , 5.803700e+01 , 5.803700e+01 , 5.803800e+01};
    			C8s = new double[] {1.969744e+03 , 1.950464e+03 , 1.947767e+03 , 1.953462e+03 , 1.963470e+03 , 1.975152e+03 , 1.987615e+03 , 1.999954e+03 , 2.011693e+03 , 2.022570e+03 , 2.032489e+03 , 2.041385e+03 , 2.049259e+03 , 2.055669e+03 , 2.061336e+03 , 2.066436e+03 , 2.070731e+03 , 2.072613e+03 , 2.072974e+03 , 2.073329e+03 , 2.073678e+03 , 2.074021e+03 , 2.074342e+03 , 2.074673e+03 , 2.074997e+03 , 2.075315e+03 , 2.075628e+03 , 2.075934e+03 , 2.076234e+03 , 2.076528e+03 , 2.076817e+03 , 2.077099e+03 , 2.077377e+03 , 2.077648e+03 , 2.077915e+03 , 2.078177e+03 , 2.078434e+03 , 2.078609e+03 , 2.078856e+03 , 2.079098e+03 , 2.079315e+03 , 2.079548e+03 , 2.079777e+03 , 2.081805e+03 , 2.083440e+03 , 2.084783e+03 , 2.085818e+03 , 2.086730e+03 , 2.087533e+03 , 2.088188e+03 , 2.088746e+03 , 2.089232e+03 , 2.089660e+03 , 2.090078e+03 , 2.090424e+03 , 2.090681e+03 , 2.090950e+03 , 2.091097e+03 , 2.091262e+03 , 2.091429e+03 , 2.091516e+03 , 2.091602e+03 , 2.091681e+03 , 2.091714e+03 , 2.091757e+03 , 2.091788e+03 , 2.091772e+03 , 2.091800e+03 , 2.091807e+03 , 2.091794e+03 , 2.091819e+03 , 2.091702e+03 , 2.091662e+03 , 2.091695e+03 , 2.091673e+03 , 2.091601e+03 , 2.091576e+03 , 2.091563e+03 , 2.091560e+03 , 2.091566e+03 , 2.091555e+03 , 2.091545e+03 , 2.091531e+03 , 2.091514e+03 , 2.091497e+03 , 2.091480e+03 , 2.091432e+03 , 2.091430e+03 , 2.091418e+03 , 2.091415e+03 , 2.091393e+03 , 2.091369e+03 , 2.091365e+03 , 2.091361e+03 , 2.091357e+03 , 2.091365e+03 , 2.091349e+03 , 2.091359e+03 , 2.091342e+03 , 2.091345e+03 , 2.091343e+03 , 2.091331e+03 , 2.091334e+03 , 2.091337e+03 , 2.091305e+03 , 2.091306e+03 , 2.091304e+03 , 2.091288e+03 , 2.091272e+03 , 2.091227e+03 , 2.091227e+03 , 2.091243e+03 , 2.091258e+03 , 2.091268e+03 , 2.091291e+03}; 
    			C10s = new double[] {7.115940e+04 , 6.876290e+04 , 6.764970e+04 , 6.720700e+04 , 6.714790e+04 , 6.725760e+04 , 6.750110e+04 , 6.780390e+04 , 6.812880e+04 , 6.845440e+04 , 6.877200e+04 , 6.907140e+04 , 6.934720e+04 , 6.953240e+04 , 6.971570e+04 , 6.991140e+04 , 7.007830e+04 , 7.015330e+04 , 7.016840e+04 , 7.018320e+04 , 7.019790e+04 , 7.021230e+04 , 7.022430e+04 , 7.023830e+04 , 7.025210e+04 , 7.026560e+04 , 7.027900e+04 , 7.029220e+04 , 7.030510e+04 , 7.031780e+04 , 7.033040e+04 , 7.034270e+04 , 7.035480e+04 , 7.036680e+04 , 7.037860e+04 , 7.039020e+04 , 7.040170e+04 , 7.040260e+04 , 7.041370e+04 , 7.042470e+04 , 7.043240e+04 , 7.044310e+04 , 7.045360e+04 , 7.054840e+04 , 7.062770e+04 , 7.069960e+04 , 7.075360e+04 , 7.081110e+04 , 7.086760e+04 , 7.091560e+04 , 7.095790e+04 , 7.099660e+04 , 7.103290e+04 , 7.107100e+04 , 7.110320e+04 , 7.112550e+04 , 7.115190e+04 , 7.116430e+04 , 7.118050e+04 , 7.119830e+04 , 7.120670e+04 , 7.121570e+04 , 7.122410e+04 , 7.122740e+04 , 7.123180e+04 , 7.123520e+04 , 7.123190e+04 , 7.123470e+04 , 7.123500e+04 , 7.123330e+04 , 7.123700e+04 , 7.122070e+04 , 7.121420e+04 , 7.121950e+04 , 7.121590e+04 , 7.120620e+04 , 7.120220e+04 , 7.120070e+04 , 7.120060e+04 , 7.120260e+04 , 7.120010e+04 , 7.119790e+04 , 7.119580e+04 , 7.119290e+04 , 7.119080e+04 , 7.118660e+04 , 7.117820e+04 , 7.117730e+04 , 7.117400e+04 , 7.116990e+04 , 7.116640e+04 , 7.115830e+04 , 7.115610e+04 , 7.115310e+04 , 7.115150e+04 , 7.115190e+04 , 7.114870e+04 , 7.114960e+04 , 7.114690e+04 , 7.114700e+04 , 7.114640e+04 , 7.114450e+04 , 7.114480e+04 , 7.114500e+04 , 7.114010e+04 , 7.114020e+04 , 7.114000e+04 , 7.113820e+04 , 7.113770e+04 , 7.113470e+04 , 7.113440e+04 , 7.113520e+04 , 7.113620e+04 , 7.113680e+04 , 7.113830e+04 };
    			Rcs = new double[] {3.1940 , 3.1660 , 3.1480 , 3.1380 , 3.1300 , 3.1260 , 3.1240 , 3.1220 , 3.1200 , 3.1200 , 3.1200 , 3.1200 , 3.1220 , 3.1220 , 3.1220 , 3.1220 , 3.1220 , 3.1220 , 3.1240 , 3.1240 , 3.1240 , 3.1240 , 3.1240 , 3.1240 , 3.1240 , 3.1240 , 3.1240 , 3.1240 , 3.1240 , 3.1240 , 3.1240 , 3.1240 , 3.1240 , 3.1240 , 3.1240 , 3.1240 , 3.1240 , 3.1240 , 3.1240 , 3.1240 , 3.1240 , 3.1240 , 3.1240 , 3.1240 , 3.1260 , 3.1260 , 3.1260 , 3.1280 , 3.1280 , 3.1280 , 3.1280 , 3.1300 , 3.1300 , 3.1300 , 3.1300 , 3.1300 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320 , 3.1320};
    		} else if (basis == 2) {
    	    //aug-cc-pVDZ
    			u12NoDisp = new double[] {1.740610210999876e-01 , 1.247698032000244e-01 , 8.883397080012401e-02 , 6.280313649995151e-02 , 4.406264240014934e-02 , 3.065882710006917e-02 , 2.114190520001102e-02 , 1.443893420014319e-02 , 9.758016600017072e-03 , 6.517940600133443e-03 , 4.295655000078114e-03 , 2.786052599958566e-03 , 1.771275800138028e-03 , 1.097253900070427e-03 , 6.552749000547919e-04 , 3.692975001285959e-04 , 1.881364000837493e-04 , 1.250105001417978e-04 , 1.142242001606064e-04 , 1.039808000768971e-04 , 9.424660015611153e-05 , 8.498850002069958e-05 , 7.617530013703799e-05 , 6.777940006941208e-05 , 5.977809996693395e-05 , 5.215410010350752e-05 , 4.489570005716814e-05 , 3.799570004048292e-05 , 3.145000005133625e-05 , 2.525540003261995e-05 , 1.940820015988720e-05 , 1.390010015711596e-05 , 8.721299991520937e-06 , 3.857699994114228e-06 , -7.076998826960335e-07 , -4.995200015400769e-06 , -9.025199915413396e-06 , -1.281879985981504e-05 , -1.639619995330577e-05 , -1.977540000552835e-05 , -2.297080004609597e-05 , -2.599300000838412e-05 , -2.884869991248706e-05 , -4.858080001213239e-05 , -5.665460003001499e-05 , -5.834919988956244e-05 , -5.630929990729783e-05 , -5.282729989630752e-05 , -4.846429987992451e-05 , -4.440249995241174e-05 , -4.027699992548150e-05 , -3.666520001388562e-05 , -3.320859991617908e-05 , -2.998829995704000e-05 , -2.701120001802337e-05 , -2.408149998700537e-05 , -2.140039987352793e-05 , -1.881920002233528e-05 , -1.646259988774545e-05 , -1.430049996997695e-05 , -1.230049997502647e-05 , -1.057109989233140e-05 , -9.002900014820625e-06 , -7.616099992446834e-06 , -6.450000000768341e-06 , -5.397499990067445e-06 , -4.486599891606602e-06 , -3.751099939108826e-06 , -3.080499936913839e-06 , -2.487799974915106e-06 , -2.041299921984319e-06 , -1.646900045670918e-06 , -1.271600012842100e-06 , -9.967000096366974e-07 , -8.008998975128634e-07 , -5.966999196971301e-07 , -4.205999175610486e-07 , -3.294999260106124e-07 , -2.660999598447233e-07 , -1.797000095393742e-07 , -1.132998477260116e-07 , -1.079999947251054e-07 , -1.183000222226838e-07 , -9.349992069473956e-08 , -6.810000741097610e-08 , -8.469987733406015e-08 , -1.267999323317781e-07 , -1.347000306850532e-07 , -1.138998868555063e-07 , -1.134999365604017e-07 , -1.496000550105236e-07 , -1.821999831008725e-07 , -1.705000158835901e-07 , -1.420999069523532e-07 , -1.398000222252449e-07 , -1.693999820417957e-07 , -1.912999323394615e-07 , -1.691998932074057e-07 , -1.285000053030672e-07 , -1.127998530137120e-07 , -1.306000285694608e-07 , -1.541000074212207e-07 , -1.409998731105588e-07 , -9.480004337092396e-08 , -4.219987204123754e-08 , 8.500001058564521e-08 , 4.460002855921630e-08 , 8.830011211102828e-08 , 1.094399976864224e-06 , 1.125599965234869e-06 , 7.564001407445176e-07 , 4.988000910088886e-07 , 3.324000772408908e-07 , 0.000000000000000e+00 , 0.000000000000000e+00};
    			C6s = new double[] {5.377500e+01 , 5.376300e+01 , 5.401200e+01 , 5.437800e+01 , 5.478500e+01 , 5.519700e+01 , 5.559200e+01 , 5.596000e+01 , 5.629500e+01 , 5.659500e+01 , 5.686100e+01 , 5.709300e+01 , 5.729200e+01 , 5.746200e+01 , 5.760200e+01 , 5.771700e+01 , 5.780900e+01 , 5.784700e+01 , 5.785400e+01 , 5.786100e+01 , 5.786700e+01 , 5.787400e+01 , 5.788000e+01 , 5.788600e+01 , 5.789200e+01 , 5.789800e+01 , 5.790400e+01 , 5.790900e+01 , 5.791500e+01 , 5.792000e+01 , 5.792500e+01 , 5.792900e+01 , 5.793400e+01 , 5.793900e+01 , 5.794300e+01 , 5.794700e+01 , 5.795100e+01 , 5.795500e+01 , 5.795900e+01 , 5.796300e+01 , 5.796700e+01 , 5.797000e+01 , 5.797400e+01 , 5.800100e+01 , 5.802000e+01 , 5.803200e+01 , 5.803800e+01 , 5.804200e+01 , 5.804200e+01 , 5.804200e+01 , 5.804000e+01 , 5.803800e+01 , 5.803600e+01 , 5.803400e+01 , 5.803100e+01 , 5.803000e+01 , 5.802800e+01 , 5.802700e+01 , 5.802700e+01 , 5.802600e+01 , 5.802600e+01 , 5.802600e+01 , 5.802600e+01 , 5.802700e+01 , 5.802700e+01 , 5.802800e+01 , 5.802800e+01 , 5.802900e+01 , 5.802900e+01 , 5.803000e+01 , 5.803000e+01 , 5.803100e+01 , 5.803100e+01 , 5.803200e+01 , 5.803200e+01 , 5.803200e+01 , 5.803200e+01 , 5.803200e+01 , 5.803200e+01 , 5.803200e+01 , 5.803100e+01 , 5.803000e+01 , 5.803000e+01 , 5.803000e+01 , 5.802900e+01 , 5.802900e+01 , 5.802800e+01 , 5.802700e+01 , 5.802700e+01 , 5.802600e+01 , 5.802600e+01 , 5.802400e+01 , 5.802400e+01 , 5.802400e+01 , 5.802300e+01 , 5.802300e+01 , 5.802200e+01 , 5.802200e+01 , 5.802200e+01 , 5.802200e+01 , 5.802200e+01 , 5.802200e+01 , 5.802200e+01 , 5.802200e+01 , 5.802200e+01 , 5.802200e+01 , 5.802200e+01 , 5.802100e+01 , 5.802100e+01 , 5.802000e+01 , 5.802000e+01 , 5.802100e+01 , 5.802100e+01 , 5.802200e+01 , 5.802200e+01};
    			C8s = new double[] {1.988745e+03 , 1.963829e+03 , 1.958264e+03 , 1.962525e+03 , 1.971652e+03 , 1.983132e+03 , 1.995684e+03 , 2.008191e+03 , 2.020150e+03 , 2.031328e+03 , 2.041508e+03 , 2.050627e+03 , 2.058685e+03 , 2.065677e+03 , 2.071655e+03 , 2.076678e+03 , 2.080851e+03 , 2.082674e+03 , 2.083018e+03 , 2.083356e+03 , 2.083673e+03 , 2.083999e+03 , 2.084304e+03 , 2.084617e+03 , 2.084914e+03 , 2.085215e+03 , 2.085510e+03 , 2.085799e+03 , 2.086082e+03 , 2.086332e+03 , 2.086603e+03 , 2.086870e+03 , 2.087131e+03 , 2.087388e+03 , 2.087639e+03 , 2.087886e+03 , 2.088128e+03 , 2.088366e+03 , 2.088599e+03 , 2.088828e+03 , 2.089053e+03 , 2.089273e+03 , 2.089489e+03 , 2.091424e+03 , 2.092998e+03 , 2.094276e+03 , 2.095362e+03 , 2.096221e+03 , 2.096914e+03 , 2.097478e+03 , 2.097941e+03 , 2.098278e+03 , 2.098512e+03 , 2.098718e+03 , 2.098842e+03 , 2.098908e+03 , 2.098984e+03 , 2.099028e+03 , 2.099053e+03 , 2.099046e+03 , 2.099077e+03 , 2.099082e+03 , 2.099087e+03 , 2.099058e+03 , 2.099030e+03 , 2.099044e+03 , 2.099014e+03 , 2.098989e+03 , 2.098984e+03 , 2.098976e+03 , 2.098937e+03 , 2.098907e+03 , 2.098892e+03 , 2.098925e+03 , 2.098945e+03 , 2.098964e+03 , 2.098903e+03 , 2.098946e+03 , 2.099004e+03 , 2.098992e+03 , 2.098954e+03 , 2.098980e+03 , 2.099021e+03 , 2.099079e+03 , 2.099139e+03 , 2.099198e+03 , 2.099257e+03 , 2.099286e+03 , 2.099316e+03 , 2.099374e+03 , 2.099389e+03 , 2.099314e+03 , 2.099305e+03 , 2.099336e+03 , 2.099349e+03 , 2.099364e+03 , 2.099361e+03 , 2.099345e+03 , 2.099332e+03 , 2.099370e+03 , 2.099397e+03 , 2.099377e+03 , 2.099414e+03 , 2.099438e+03 , 2.099411e+03 , 2.099403e+03 , 2.099425e+03 , 2.099412e+03 , 2.099395e+03 , 2.099359e+03 , 2.099343e+03 , 2.099355e+03 , 2.099378e+03 , 2.099429e+03 , 2.099429e+03};
    			C10s = new double[] {7.410670e+04 , 7.116980e+04 , 6.978160e+04 , 6.920680e+04 , 6.905840e+04 , 6.915590e+04 , 6.941750e+04 , 6.973860e+04 , 7.007950e+04 , 7.042480e+04 , 7.075180e+04 , 7.105490e+04 , 7.133260e+04 , 7.157980e+04 , 7.179700e+04 , 7.198350e+04 , 7.214280e+04 , 7.221690e+04 , 7.223120e+04 , 7.224530e+04 , 7.225710e+04 , 7.227080e+04 , 7.228220e+04 , 7.229560e+04 , 7.230740e+04 , 7.232050e+04 , 7.233340e+04 , 7.234610e+04 , 7.235860e+04 , 7.236670e+04 , 7.237890e+04 , 7.239100e+04 , 7.240290e+04 , 7.241470e+04 , 7.242630e+04 , 7.243780e+04 , 7.244920e+04 , 7.246050e+04 , 7.247170e+04 , 7.248270e+04 , 7.249370e+04 , 7.250450e+04 , 7.251510e+04 , 7.261520e+04 , 7.270130e+04 , 7.277640e+04 , 7.284760e+04 , 7.290660e+04 , 7.295700e+04 , 7.300190e+04 , 7.304330e+04 , 7.307400e+04 , 7.309750e+04 , 7.312110e+04 , 7.313680e+04 , 7.314670e+04 , 7.315930e+04 , 7.316900e+04 , 7.317640e+04 , 7.317880e+04 , 7.318790e+04 , 7.319270e+04 , 7.319780e+04 , 7.319820e+04 , 7.319830e+04 , 7.320480e+04 , 7.320490e+04 , 7.320550e+04 , 7.320900e+04 , 7.321230e+04 , 7.321060e+04 , 7.321020e+04 , 7.321170e+04 , 7.321990e+04 , 7.322590e+04 , 7.323120e+04 , 7.322350e+04 , 7.323020e+04 , 7.323800e+04 , 7.323400e+04 , 7.322550e+04 , 7.322460e+04 , 7.322480e+04 , 7.322670e+04 , 7.322830e+04 , 7.322840e+04 , 7.322870e+04 , 7.322440e+04 , 7.322080e+04 , 7.322130e+04 , 7.321610e+04 , 7.319650e+04 , 7.318920e+04 , 7.318750e+04 , 7.318270e+04 , 7.318030e+04 , 7.317490e+04 , 7.316930e+04 , 7.316480e+04 , 7.316880e+04 , 7.317160e+04 , 7.316770e+04 , 7.317250e+04 , 7.317520e+04 , 7.316970e+04 , 7.316880e+04 , 7.317200e+04 , 7.317040e+04 , 7.316980e+04 , 7.316820e+04 , 7.316600e+04 , 7.316650e+04 , 7.316830e+04 , 7.317230e+04 , 7.317230e+04};
    			Rcs = new double[] {3.2240 , 3.1920 , 3.1720 , 3.1600 , 3.1540 , 3.1480 , 3.1460 , 3.1440 , 3.1440 , 3.1440 , 3.1420 , 3.1440 , 3.1440 , 3.1440 , 3.1440 , 3.1440 , 3.1460 , 3.1460 , 3.1460 , 3.1460 , 3.1460 , 3.1460 , 3.1460 , 3.1460 , 3.1460 , 3.1460 , 3.1460 , 3.1460 , 3.1460 , 3.1460 , 3.1460 , 3.1460 , 3.1460 , 3.1460 , 3.1460 , 3.1460 , 3.1460 , 3.1460 , 3.1460 , 3.1460 , 3.1460 , 3.1480 , 3.1480 , 3.1480 , 3.1480 , 3.1500 , 3.1500 , 3.1500 , 3.1500 , 3.1520 , 3.1520 , 3.1520 , 3.1520 , 3.1520 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540 , 3.1540};

    		} else {
    			throw new RuntimeException("Specify different basis.");
    		}
    	        
	    	//System.out.println("r12 = "+r+" Angstroms, u12 = " + energy + " Hartrees");
    	
    		int n = 0;
    		
    		for (int i=0;i<rs.length; i++) {
    			
    			if (r == rs[i]) {
    				n = i;
    			}	
    			/*
    			if (rs[i]==12) {
    				System.out.println(i);
    			}
    			*/
    		}
    		
    		if (r > 10) {
    			
    			
    			//n = 83; // 8 Angstroms
    			n = 103; // 10 Angstroms
    			//n = 105; // 12 Angstroms
	    		
    		} 
    		
    		if (n != 0) {
    			
    			energy = u12NoDisp[n];
				C6 = C6s[n];
				C8 = C8s[n];
				C10 = C10s[n];
				Rc = Rcs[n];
    			
    		}
    		else {
	
    		
	    		AkimaSpline splineU = new AkimaSpline();
	    		splineU.setInputData(rs, u12NoDisp);
	    		double[] rA = new double[] {r};
	    		double[] energyA = splineU.doInterpolation(rA);
	    		energy = energyA[0];
	    		
	    		AkimaSpline spline6 = new AkimaSpline();
	    		splineU.setInputData(rs, C6s);
	    		double[] C6A = splineU.doInterpolation(rA);
	    		C6 = C6A[0];
	    		
	    		AkimaSpline spline8 = new AkimaSpline();
	    		splineU.setInputData(rs, C8s);
	    		double[] C8A = splineU.doInterpolation(rA);
	    		C8 = C8A[0];
	    		
	    		AkimaSpline spline10 = new AkimaSpline();
	    		splineU.setInputData(rs, C10s);
	    		double[] C10A = splineU.doInterpolation(rA);
	    		C10 = C10A[0];
	    		
	    		AkimaSpline splineRc = new AkimaSpline();
	    		splineU.setInputData(rs, Rcs);
	    		double[] RcA = splineU.doInterpolation(rA);
	    		Rc = RcA[0];
    		}
    	
    		
    		//System.out.println(energy);
    		if (!fixedRvdw) {
    			Rvdw = (a1*Rc + a2)/100;
    		}
			double energy6  =  -C6/(Math.pow(Rvdw,6) + Math.pow(r,6)) *Math.pow(0.529177209,6);
			double energy8  =  -C8/(Math.pow(Rvdw,8) + Math.pow(r,8)) *Math.pow(0.529177209,8);
			double energy10 = -C10/(Math.pow(Rvdw,10)+ Math.pow(r,10))*Math.pow(0.529177209,10);
			double dispEnergy = energy6 + energy8 + energy10; //Hartrees
			if (r > 8) {
				energy = dispEnergy;
			} else {
				energy = energy + dispEnergy;//Hartrees
			}
			
    	
    	}
    	
    	//double energy2 = energy*2625.5; //convert to kJ/mol
    	//energy2 = energy2*1000; //convert to J/mol
    	//double R = k*Na;
		//double energyOverR = energy2/R; // Kelvin
		//System.out.println("u12/R (K) = " + energyOverR);
    	
    	energy = energy*JPerHartree; // Joules;
    	double energyOverkB = energy/k;//Kelvin
		//System.out.println("u12/k (K) = " + energyOverkB + " "+ R);
		return energyOverkB;
    }

    /**
     * The derivative r*du/dr.
     */
    public double du(double r2) {
        
        return 0;
    }

   /**
    * The second derivative of the pair energy, times the square of the
    * separation:  r^2 d^2u/dr^2.
    */
    public double d2u(double r2) {
     
        return 0;
    }
            
    /**
     *  Integral used for corrections to potential truncation.
     */
    public double uInt(double rC) {
        
        return 0;  //complete LRC is obtained by multiplying by N1*N2/V
    }
    
    

    public static void main(String[] args)  {
    	
		DampingParams params = new DampingParams();
	    
		   
		if (args.length == 5 ) {
			params.a1 = Integer.parseInt(args[0]);
			params.a2 = Integer.parseInt(args[1]);
			params.RvdwF = Double.parseDouble(args[2]);
			params.basis = Integer.parseInt(args[3]);
			params.fixedRvdw = Boolean.parseBoolean(args[4]);
	    } 
		
		int a1 = params.a1;
		int a2 = params.a2;
		double RvdwF = params.RvdwF;
		int basis = params.basis;
		boolean fixedRvdw = params.fixedRvdw;
		
		
	
		Space space = Space3D.getInstance();
    	P2QChemInterpolated p2 = new P2QChemInterpolated(space);
    	
    	p2.setDampingParams(a1,a2,RvdwF, basis, fixedRvdw);
    	double r = 3.6;
    	double u;
    	double umin=0;
    	double rmin=0;
    	
    	while (r<4) {
    		r = r + 0.00001;
    		u = p2.u(r*r); // Kelvin
    		u = u*k/JPerHartree*1e6;
    		if (u < umin) {
    			umin = u;
    			rmin =r;
    		}
    		//System.out.println(Rvdw+"  "+r+"  "+u);
    	}
    	
    	//Minimum energy
    	System.out.println(Rvdw+"	"+rmin+ "    " +umin);
    	
    	
    	
    	
    	//r=3.757178;
    	r=12;
    	System.out.println("r12=	"+r + ", u12 =    " +p2.u(r*r)*k/JPerHartree*1e6);
    	
    	
    }
   
	public static class DampingParams extends ParameterBase {
		/*
		protected int a1 = 79;	        
	    protected int a2 = 136;   
	    protected int basis = 3;
	    */
	    
	    protected int a1 = 80;	        
	    protected int a2 = 149;   
	    protected int basis = 2;
	    
	    protected double RvdwF = 3.828;   
	     
	    protected boolean fixedRvdw = false; 
	    
	 

	}
   
	protected int a1;	        
    protected int a2;   
    protected int basis;
    protected double RvdwF;   
    protected static double Rvdw; 
    protected boolean fixedRvdw ; 
    protected static double k = 1.3806503e-23; //J/K   1.3806503e-23
    protected static double JPerHartree = 4.359744e-18;  //4.359744e-18
    protected static double Na = 6.0221415e23;  
    
    
    }
