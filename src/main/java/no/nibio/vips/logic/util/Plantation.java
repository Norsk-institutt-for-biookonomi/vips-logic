
package no.nibio.vips.logic.util;

import java.util.Date;
import java.util.List;

/**
 *
 * @author wildfly
 */


public class Plantation {
    private String  typeName;
    private Double  baseTemp;
    List<PhaseInfo>   phaseInfo;
    
    public Plantation() {
    }

    public Plantation(String typeName, List<PhaseInfo> phaseInfo) {
        this.typeName   = typeName;
        this.phaseInfo  = phaseInfo;
        } 
    
      public Plantation(String typeName,Double baseTemp, List<PhaseInfo> phaseInfo) {
        this.typeName   = typeName;
        this.baseTemp  = baseTemp;
        this.phaseInfo  = phaseInfo;
        }  

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Double getBaseTemp() {
        return baseTemp;
    }

    public void setBaseTemp(Double baseTemp) {
        this.baseTemp = baseTemp;
    }

    public List<PhaseInfo> getPhaseInfo() {
        return phaseInfo;
    }

    public void setPhaseInfo(List<PhaseInfo> phaseInfo) {
        this.phaseInfo = phaseInfo;
    }

      
    @Override
    public String toString() {
        return "Plantation{" + "typeName=" + typeName + ", baseTemp=" + baseTemp + ", phaseInfo=" + phaseInfo + '}';
    }
    
    public class PhaseInfo
    {
            private String  phaseName;
            private Double  heatReq;

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
            return "PhaseInfo{" + "phaseName=" + phaseName + ", heatReq=" + heatReq + '}';
        }
        
    }
    
    
}

