package no.nibio.vips.logic.util;

import java.util.Date;
import java.util.List;

/**
 *
 * @author wildfly
 */


public class Plant {
    private String              speciesLatinName;
    private Double              baseTemp;
    private Date                startDate;
    private List<Plantation>    typeNames;

    public String getSpeciesLatinName() {
        return speciesLatinName;
    }
    public void setSpeciesLatinName(String speciesLatinName) {
        this.speciesLatinName = speciesLatinName;
    }
    public Double getBaseTemp() {
        return baseTemp;
    }
    public void setBaseTemp(Double baseTemp) {
        this.baseTemp = baseTemp;
    }
    public Date getStartDate() {
        return startDate;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    public List<Plantation> getTypeNames() {
        return typeNames;
    }
    public void setTypeNames(List<Plantation> typeNames) {
        this.typeNames = typeNames;
    }

    @Override
    public String toString() {
        return "Plant{" + "speciesLatinName=" + speciesLatinName + ", baseTemp=" + baseTemp + ", startDate=" + startDate + ", typeNames=" + typeNames + '}';
    }
    
}
