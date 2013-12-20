/*
 * Copyright (c) 2013 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with VIPSLogic.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.entity;

import java.io.Serializable;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Entity
@Table(name = "task_history_status")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TaskHistoryStatus.findAll", query = "SELECT t FROM TaskHistoryStatus t"),
    @NamedQuery(name = "TaskHistoryStatus.findByTaskHistoryStatusId", query = "SELECT t FROM TaskHistoryStatus t WHERE t.taskHistoryStatusId = :taskHistoryStatusId"),
    @NamedQuery(name = "TaskHistoryStatus.findByDefaultTitle", query = "SELECT t FROM TaskHistoryStatus t WHERE t.defaultTitle = :defaultTitle")})
public class TaskHistoryStatus implements Serializable {
    public final static Integer STATUS_OK = 1;
    public final static Integer STATUS_FAILED = 2;
    
    
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "task_history_status_id")
    private Integer taskHistoryStatusId;
    @Size(max = 63)
    @Column(name = "default_title")
    private String defaultTitle;
    @OneToMany(mappedBy = "taskHistoryStatusId")
    private Set<TaskHistory> taskHistorySet;

    public TaskHistoryStatus() {
    }

    public TaskHistoryStatus(Integer taskHistoryStatusId) {
        this.taskHistoryStatusId = taskHistoryStatusId;
    }

    public Integer getTaskHistoryStatusId() {
        return taskHistoryStatusId;
    }

    public void setTaskHistoryStatusId(Integer taskHistoryStatusId) {
        this.taskHistoryStatusId = taskHistoryStatusId;
    }

    public String getDefaultTitle() {
        return defaultTitle;
    }

    public void setDefaultTitle(String defaultTitle) {
        this.defaultTitle = defaultTitle;
    }

    @XmlTransient
    @JsonIgnore
    public Set<TaskHistory> getTaskHistorySet() {
        return taskHistorySet;
    }

    public void setTaskHistorySet(Set<TaskHistory> taskHistorySet) {
        this.taskHistorySet = taskHistorySet;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (taskHistoryStatusId != null ? taskHistoryStatusId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TaskHistoryStatus)) {
            return false;
        }
        TaskHistoryStatus other = (TaskHistoryStatus) object;
        if ((this.taskHistoryStatusId == null && other.taskHistoryStatusId != null) || (this.taskHistoryStatusId != null && !this.taskHistoryStatusId.equals(other.taskHistoryStatusId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.entity.TaskHistoryStatus[ taskHistoryStatusId=" + taskHistoryStatusId + " ]";
    }

}
