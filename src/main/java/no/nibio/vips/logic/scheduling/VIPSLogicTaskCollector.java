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

import java.util.ArrayList;
import java.util.List;

/**
 * Idea: One Task Collector per organization
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class VIPSLogicTaskCollector {
    private TaskTable taskTable;
    private Integer organizationId;
    
    public VIPSLogicTaskCollector(Integer organizationId)
    {
        this.organizationId = organizationId;
    }

    public TaskTable getTasks() {
        if(this.taskTable == null)
            this.taskTable = new TaskTable();
        return this.taskTable;
    }
    
    /**
     * Intention: Reveal which organization this task collector is for.
     * @return 
     */
    public Integer getOrganizationId(){
        return this.organizationId;
    }

    /**
     * Replaces cron4j TaskTable.
     */
    public static class TaskTable {
        private final List<String> cronPatterns = new ArrayList<>();
        private final List<VipsLogicTask> tasks = new ArrayList<>();

        public void add(String cronPattern, VipsLogicTask task) {
            cronPatterns.add(cronPattern);
            tasks.add(task);
        }

        public int size() {
            return tasks.size();
        }

        public String getCronPattern(int index) {
            return cronPatterns.get(index);
        }

        public VipsLogicTask getTask(int index) {
            return tasks.get(index);
        }
    }
}
