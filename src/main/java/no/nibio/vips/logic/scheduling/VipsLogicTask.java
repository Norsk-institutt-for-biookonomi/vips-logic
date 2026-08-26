/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package no.nibio.vips.logic.scheduling;

import java.util.Map;
import no.nibio.vips.i18n.I18nImpl;
import no.nibio.vips.logic.entity.Organization;

/**
 * Adding some metadata properties to the task
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public abstract class VipsLogicTask {
    /** Default language is English */
    public static final String DEFAULT_LANGUAGE = "en";
    
    private final I18nImpl i18n;
    private Map<String,String[]> configuration;
    private Integer factoryId;
    private Organization organization;
    
    public abstract String getConfigFormDefinition(String language);

    public abstract void execute(TaskExecutionContext tec) throws RuntimeException;

    public boolean supportsStatusTracking() {
        return false;
    }

    public boolean supportsCompletenessTracking() {
        return false;
    }

    /**
     * Replaces cron4j TaskExecutionContext.
     */
    public static class TaskExecutionContext {
        private final TaskExecutor taskExecutor;

        public TaskExecutionContext(TaskExecutor taskExecutor) {
            this.taskExecutor = taskExecutor;
        }

        public TaskExecutor getTaskExecutor() {
            return taskExecutor;
        }

        public void setCompleteness(double completeness) {
            taskExecutor.setCompleteness(completeness);
        }

        public void setStatusMessage(String statusMessage) {
            taskExecutor.setStatusMessage(statusMessage);
        }
    }

    /**
     * Replaces cron4j TaskExecutor.
     */
    public static class TaskExecutor {
        public final long startTime;
        private final VipsLogicTask task;
        private double completeness;
        private String statusMessage;

        public TaskExecutor(VipsLogicTask task) {
            this.task = task;
            this.startTime = System.currentTimeMillis();
        }

        public VipsLogicTask getTask() {
            return task;
        }

        public long getStartTime() {
            return startTime;
        }

        public double getCompleteness() {
            return completeness;
        }

        public void setCompleteness(double completeness) {
            this.completeness = completeness;
        }

        public String getStatusMessage() {
            return statusMessage;
        }

        public void setStatusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
        }
    }
    
    /**
     * Initiates i18n module. Using no.nibio.vips.logic.i18n.vipslogictexts for translations
     * of name and description
     */
    public VipsLogicTask()
    {
        super();
        this.i18n = new I18nImpl("no.nibio.vips.logic.i18n.vipslogictexts");
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
     * * Add to translation in "no.nibio.vips.logic.i18n.vipslogictexts" with key = "task_" + [ClassName] + "_name"
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
     * Add to translation in "no.nibio.vips.logic.i18n.vipslogictexts" with key = "task_" + [ClassName] + "_description"
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

    /**
     * @return the organizationId
     */
    public Organization getOrganization() {
        return organization;
    }

    /**
     * @param organizationId the organizationId to set
     */
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
}
