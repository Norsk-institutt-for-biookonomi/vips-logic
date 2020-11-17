package no.nibio.vips.logic.util;

import java.util.Date;
import java.util.List;

/**
 *
 * @author wildfly
 */


public class Plant {
    private String              speciesLatinName;
    private String              typeName;
    private Double              baseTemp;
    private Date                startDate;
    private String[]            weatherParams;
    
    private List<Plantation>    plantations;

    public String getSpeciesLatinName() {
        return speciesLatinName;
    }

    public void setSpeciesLatinName(String speciesLatinName) {
        this.speciesLatinName = speciesLatinName;
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public List<Plantation> getPlantations() {
        return plantations;
    }

    public void setPlantations(List<Plantation> plantations) {
        this.plantations = plantations;
    }

    public String[] getWeatherParams() {
        return weatherParams;
    }

    public void setWeatherParams(String[] weatherParams) {
        this.weatherParams = weatherParams;
    }

    @Override
    public String toString() {
        return "Plant{" + "speciesLatinName=" + speciesLatinName + ", typeName=" + typeName + ", baseTemp=" + baseTemp + ", startDate=" + startDate + ", weatherParams=" + weatherParams + ", plantations=" + plantations + '}';
    }


        
}
