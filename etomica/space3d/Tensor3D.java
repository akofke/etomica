package etomica.space3d;


import etomica.space.Tensor;
import etomica.space.Vector;
import etomica.utility.Function;

/*
 * History
 * Created on Jan 24, 2005 by kofke
 */
public class Tensor3D implements Tensor, java.io.Serializable {

    double xx, xy, xz, yx, yy, yz, zx, zy, zz;

    public Tensor3D () {xx = xy = xz = yx = yy = yz = zx = zy = zz = 0.0;}
    public Tensor3D (double[] d) {
        this.E(d);
    }
    
    public double[] toArray() {
        return new double[] {xx, xy, xz, yx, yy, yz, zx, zy, zz};
    }

    /**
     * Support of implementation of Cloneable interface. Returns a new Tensor
     * with elements equal to this one.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new InternalError(ex.toString());
        }
    }

    public double component(int i, int j) {
        return ( i==0 ) ? ( (j==0) ? xx : ( j==1 ? xy : xz ) ) : ( (i==1) ? ( (j==0) ? yx : ( (j==1) ? yy : yz ) ) : ( (j==0) ? zx : ((j==1) ? zy : zz)));
    }
    public int length() {return 3;}
    public void setComponent(int i, int j, double d) {
        if (i==0) {if (j==0) {xx = d;} else if (j==1) {xy = d;} else xz = d;}
        else if (i==1) {if (j==0) {yx = d;} else if (j==1) {yy=d;} else yz = d;}
        else {if (j==0) {zx = d;} else if (j==1) {zy = d;} else zz = d;}
    }
    public void E(Tensor u) {
        Tensor3D t = (Tensor3D)u;
        xx=t.xx; xy=t.xy; xz=t.xz;
        yx=t.yx; yy=t.yy; yz=t.yz;
        zx=t.zx; zy=t.zy; zz=t.zz;
    }
    public void E(Vector t1, Vector t2) {
        Vector3D u1 = (Vector3D)t1;
        Vector3D u2 = (Vector3D)t2;
        xx=u1.x*u2.x; xy=u1.x*u2.y; xz=u1.x*u2.z;
        yx=u1.y*u2.x; yy=u1.y*u2.y; yz=u1.y*u2.z;
        zx=u1.z*u2.x; zy=u1.z*u2.y; zz=u1.z*u2.z;
    }
    public void E(double a) {xx=xy=xz=yx=yy=yz=zx=zy=zz=a;}
    public void PE(double a) {
        xx+=a; xy+=a; xz+=a;
        yx+=a; yy+=a; yz+=a;
        zx+=a; zy+=a; zz+=a;
    }
    public void PE(Tensor u) {
        Tensor3D t = (Tensor3D)u;
        xx+=t.xx; xy+=t.xy; xz+=t.xz;
        yx+=t.yx; yy+=t.yy; yz+=t.yz;
        zx+=t.zx; zy+=t.zy; zz+=t.zz;
    }
    public void PE(int i, int j, double d) {
        if (i==0) {if (j==0) {xx += d;} else if (j==1) {xy += d;} else xz += d;}
        else if (i==1) {if (j==0) {yx += d;} else if (j==1) {yy += d;} else yz += d;}
        else {if (j==0) {zx += d;} else if (j==1) {zy += d;} else zz += d;}
    }
    public void ME(Tensor u) {
        Tensor3D t = (Tensor3D)u;
        xx-=t.xx; xy-=t.xy; xz-=t.xz;
        yx-=t.yx; yy-=t.yy; yz-=t.yz;
        zx-=t.zx; zy-=t.zy; zz-=t.zz;
    }
    public double trace() {return xx+yy+zz;}
    public void transpose() { 
    	double temp = 0.0;
    	temp = xy; xy = yx; yx = temp;
    	temp = xz; xz = zx; zx = temp;
    	temp = zy; zy = yz; yz = temp;    	
    }
    public void inverse() {
        double txx=xx;double txy=xy;double txz=xz;
        double tyx=yx;double tyy=yy;double tyz=yz;
        double tzx=zx;double tzy=zy;double tzz=zz;
	    double det = xx*yy*zz-xx*yz*zy-yx*xy*zz+yx*xz*zy+zx*xy*yz-zx*xz*yy;
        xx= (tyy*tzz-tyz*tzy)/det; 
        xy= -(txy*tzz-txz*tzy)/det;
        xz= (txy*tyz-txz*tyy)/det;
        yx= -(tyx*tzz-tyz*tzx)/det; 
        yy= (txx*tzz-txz*tzx)/det;
        yz= -(txx*tyz-txz*tyx)/det;
        zx= (tyx*tzy-tyy*tzx)/det;
        zy= -(txx*tzy-txy*tzx)/det;
        zz= (txx*tyy-txy*tyx)/det;                              
}
    
    public void PE(etomica.space.Vector u1, etomica.space.Vector u2) {PE(u1,u2);}
    public void TE(double a) {xx*=a; xy*=a; xz*=a; yx*=a; yy*=a; yz*=a; zx*=a; zy*=a; zz*=a;}
    public void TE(Tensor t) { Tensor3D u = (Tensor3D)t;
        double txx=xx;double txy=xy;double txz=xz;
        double tyx=yx;double tyy=yy;double tyz=yz;
        double tzx=zx;double tzy=zy;double tzz=zz;
        xx= txx*u.xx+txy*u.yx+txz*u.zx;   
        xy= txx*u.xy+txy*u.yy+txz*u.zy; 
        xz= txx*u.xz+txy*u.yz+txz*u.zz;
        yx= tyx*u.xx+tyy*u.yx+tyz*u.zx;   
        yy= tyx*u.xy+tyy*u.yy+tyz*u.zy; 
        yz= tyx*u.xz+tyy*u.yz+tyz*u.zz;
        zx= tzx*u.xx+tzy*u.yx+tzz*u.zx;   
        zy= tzx*u.xy+tzy*u.yy+tzz*u.zy; 
        zz= tzx*u.xz+tzy*u.yz+tzz*u.zz;                                         
    }
    public void DE(Tensor t) {
        Tensor3D u = (Tensor3D)t;
        xx /= u.xx; xy /= u.xy; xz /= u.xz;
        yx /= u.yx; yy /= u.yy; yz /= u.yz;
        zx /= u.zx; zy /= u.zy; zz /= u.zz;
    }
    public void E(double[] d) {
        if(d.length != 9) throw new IllegalArgumentException("Array size incorrector for tensor");
        xx = d[0]; xy = d[1]; xz = d[2];
        yx = d[3]; yy = d[4]; yz = d[5];
        zx = d[6]; zy = d[7]; zz = d[8];
    }
    public void assignTo(double[] d) {
        if(d.length != 1) throw new IllegalArgumentException("Array size incorrector for tensor");
        d[0] = xx; d[1] = xy; d[2] = xz; 
        d[3] = yx; d[4] = yy; d[5] = yz;
        d[6] = zx; d[7] = zy; d[8] = zz;
    }
    
    public boolean isNaN() {
        return Double.isNaN(xx) || Double.isNaN(xy) || Double.isNaN(xz)
            || Double.isNaN(yx) || Double.isNaN(yy) || Double.isNaN(yz)
            || Double.isNaN(zx) || Double.isNaN(zy) || Double.isNaN(zz);
    }
    
    public void map(Function f) {
        xx = f.f(xx); xy = f.f(xy); xz = f.f(xz);
        yx = f.f(yx); yy = f.f(yy); yz = f.f(yz);
        zx = f.f(zx); zy = f.f(zy); zz = f.f(zz);
    }

}
