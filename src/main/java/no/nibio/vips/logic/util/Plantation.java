
package no.nibio.vips.logic.util;

import java.util.Date;

/**
 *
 * @author wildfly
 */


public class Plantation {
     private String  varietyId;
    private String  phaseName;
    private String  baseTemp;
    private Double  heatReq;
    private Date    startDate;

    public Plantation() {
    }

    
    
    public Plantation(String varietyId, String phaseName, String baseTemp, Double heatReq) {
        this.varietyId = varietyId;
        this.phaseName = phaseName;
        this.baseTemp = baseTemp;
        this.heatReq = heatReq;
    }

    public Plantation(String varietyId, String phaseName, Double heatReq) {
        this.varietyId = varietyId;
        this.phaseName = phaseName;
        this.heatReq = heatReq;
        } 
    
      public Plantation(String phaseName, Double heatReq) {
        this.phaseName = phaseName;
        this.heatReq = heatReq;
        }  
    
    public String getVarietyId() {
        return varietyId;
    }

    public void setVarietyId(String varietyId) {
        this.varietyId = varietyId;
    }

    public String getPhaseName() {
        return phaseName;
    }

    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    public String getBaseTemp() {
        return baseTemp;
    }

    public void setBaseTemp(String baseTemp) {
        this.baseTemp = baseTemp;
    }

    public Double getHeatReq() {
        return heatReq;
    }

    public void setHeatReq(Double heatReq) {
        this.heatReq = heatReq;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Override
    public String toString() {
        return "Plantation{" + "varietyId=" + varietyId + ", phaseName=" + phaseName + ", baseTemp=" + baseTemp + ", heatSum=" + heatReq + ", sowingDate=" + startDate + '}';
    }

    

    
    

}
