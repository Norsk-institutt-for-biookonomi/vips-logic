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

package no.nibio.vips.logic.entity;

import java.io.Serializable;
import java.util.Set;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "task_history_status")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TaskHistoryStatus.findAll", query = "SELECT t FROM TaskHistoryStatus t"),
    @NamedQuery(name = "TaskHistoryStatus.findByTaskHistoryStatusId", query = "SELECT t FROM TaskHistoryStatus t WHERE t.taskHistoryStatusId = :taskHistoryStatusId"),
    @NamedQuery(name = "TaskHistoryStatus.findByDefaultTitle", query = "SELECT t FROM TaskHistoryStatus t WHERE t.defaultTitle = :defaultTitle")})
public class TaskHistoryStatus implements Serializable {
    public final static String STATUS_OK = "taskHistoryStatusOK";
    public final static String STATUS_FAILED_PARTLY = "taskHistoryStatusFailedPartly";
    public final static String STATUS_FAILED_COMPLETELY = "taskHistoryStatusFailedCompletely";
    
    
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(max = 255)
    @Column(name = "task_history_status_id")
    private String taskHistoryStatusId;
    @Size(max = 63)
    @Column(name = "default_title")
    private String defaultTitle;
    @OneToMany(mappedBy = "taskHistoryStatusId")
    private Set<TaskHistory> taskHistorySet;

    public TaskHistoryStatus() {
    }

    public TaskHistoryStatus(String taskHistoryStatusId) {
        this.taskHistoryStatusId = taskHistoryStatusId;
    }

    public String getTaskHistoryStatusId() {
        return taskHistoryStatusId;
    }

    public void setTaskHistoryStatusId(String taskHistoryStatusId) {
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
        return "no.nibio.vips.logic.entity.TaskHistoryStatus[ taskHistoryStatusId=" + taskHistoryStatusId + " ]";
    }

}
