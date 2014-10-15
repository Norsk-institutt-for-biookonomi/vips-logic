/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the Bioforsk Open Source License as published by 
 * Bioforsk, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Bioforsk Open Source License for more details.
 * 
 * You should have received a copy of the Bioforsk Open Source License
 * along with VIPSLogic.  If not, see <http://www.bioforsk.no/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.scheduling;

import it.sauronsoftware.cron4j.Task;
import java.util.Map;
import no.bioforsk.vips.i18n.I18nImpl;

/**
 * Adding some metadata properties to the task
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public abstract class VipsLogicTask extends Task{
    /** Default language is English */
    public static final String DEFAULT_LANGUAGE = "en";
    
    private final I18nImpl i18n;
    private Map<String,String[]> configuration;
    private Integer factoryId;
    
    public abstract String getConfigFormDefinition(String language);
    
    /**
     * Initiates i18n module. Using no.bioforsk.vips.logic.i18n.vipslogictexts for translations
     * of name and description
     */
    public VipsLogicTask()
    {
        super();
        this.i18n = new I18nImpl("no.bioforsk.vips.logic.i18n.vipslogictexts");
    }
    
    /**
     * 
     * @param configuration Additional configuration for the task
     */
    public void setConfiguration(Map configuration)
    {
        this.configuration = configuration;
    }
    
    /**
     * 
     * @return this tasks configuration options
     */
    public Map<String,String[]> getConfiguration()
    {
        return this.configuration;
    }
    
    /**
     * Get name of task in default language
     * @return 
     */
    public String getName()
    {
        return this.getName(DEFAULT_LANGUAGE);
    }
    
    /**
     * Get name of task in requested language (or default language if translation is not available)
     * * Add to translation in "no.bioforsk.vips.logic.i18n.vipslogictexts" with key = "task_" + [ClassName] + "_name"
     * @return 
     */
    public String getName(String language)
    {
        try
        {
            return this.i18n.getText("task_" + this.getClass().getSimpleName() + "_name", language);
        }
        catch(java.util.MissingResourceException ex)
        {
            return this.getClass().getName();
        }
    }
    
    /**
     * Get description of task in default language
     * @return 
     */
    public String getDescription()
    {
        return this.getDescription(DEFAULT_LANGUAGE);
    }
    
    /**
     * Get description of task in requested language (or default language if translation is not available)
     * Add to translation in "no.bioforsk.vips.logic.i18n.vipslogictexts" with key = "task_" + [ClassName] + "_description"
     * @return 
     */
    public String getDescription(String language)
    {
        try
        {
            return this.i18n.getText("task_" + this.getClass().getSimpleName() + "_description", language);
        }
        catch(java.util.MissingResourceException ex)
        {
            return this.i18n.getText("noDescriptionFound", language);
        }
    }

    /**
     * @return the factoryId
     */
    public int getFactoryId() {
        return factoryId;
    }

    /**
     * @param factoryId the factoryId to set
     */
    public void setFactoryId(int factoryId) {
        this.factoryId = factoryId;
    }
}
