
package no.nibio.vips.logic.util;

import java.util.Date;

/**
 *
 * @author wildfly
 */


public class Plantation {
    private String  typeName;
    private String  phaseName;
    //private String  baseTemp;
    private Double  heatReq;
    //private Date    startDate;

    public Plantation() {
    }

    public Plantation(String typeName, String phaseName, Double heatReq) {
        this.typeName = typeName;
        this.phaseName = phaseName;
        this.heatReq = heatReq;
        } 
    
      public Plantation(String phaseName, Double heatReq) {
        this.phaseName = phaseName;
        this.heatReq = heatReq;
        }  
    
    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getPhaseName() {
        return phaseName;
    }

    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    public Double getHeatReq() {
        return heatReq;
    }

    public void setHeatReq(Double heatReq) {
        this.heatReq = heatReq;
    }

    @Override
    public String toString() {
        return "Plantation{" + "typeName=" + typeName + ", phaseName=" + phaseName + ", heatReq=" + heatReq + '}';
    }

}
