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
import java.util.Date;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "task_history")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TaskHistory.findAll", query = "SELECT t FROM TaskHistory t"),
    @NamedQuery(name = "TaskHistory.findByOrganization", query = "SELECT t FROM TaskHistory t WHERE t.organization = :organization"),
    @NamedQuery(name = "TaskHistory.findByTaskHistoryId", query = "SELECT t FROM TaskHistory t WHERE t.taskHistoryId = :taskHistoryId"),
    @NamedQuery(name = "TaskHistory.findByTaskFactoryId", query = "SELECT t FROM TaskHistory t WHERE t.taskFactoryId = :taskFactoryId"),
    @NamedQuery(name = "TaskHistory.findByPeriod", query = "SELECT t FROM TaskHistory t WHERE t.startTime BETWEEN :periodStart AND :periodEnd"),
    @NamedQuery(name = "TaskHistory.findByPeriodAndOrganizationIncludingCommon", query = "SELECT t FROM TaskHistory t WHERE t.startTime BETWEEN :periodStart AND :periodEnd AND (t.organization IS NULL OR t.organization = :organization)"),
    @NamedQuery(name = "TaskHistory.findByStartTime", query = "SELECT t FROM TaskHistory t WHERE t.startTime = :startTime"),
    @NamedQuery(name = "TaskHistory.findByFinishTime", query = "SELECT t FROM TaskHistory t WHERE t.finishTime = :finishTime"),
    @NamedQuery(name = "TaskHistory.findByCompletenessAtFinish", query = "SELECT t FROM TaskHistory t WHERE t.completenessAtFinish = :completenessAtFinish"),
    @NamedQuery(name = "TaskHistory.findByMessage", query = "SELECT t FROM TaskHistory t WHERE t.message = :message")})
public class TaskHistory implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "task_history_id")
    private Long taskHistoryId;
    @Column(name = "task_factory_id")
    private Integer taskFactoryId;
    @JoinColumn(name = "organization_id", referencedColumnName = "organization_id")
    @ManyToOne
    private Organization organization;
    @Column(name = "start_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;
    @Column(name = "finish_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date finishTime;
    @Column(name = "completeness_at_finish")
    private Double completenessAtFinish;
    @Size(max = 2147483647)
    @Column(name = "message")
    private String message;
    @JoinColumn(name = "task_history_status_id", referencedColumnName = "task_history_status_id")
    @ManyToOne
    private TaskHistoryStatus taskHistoryStatusId;

    public TaskHistory() {
    }

    public TaskHistory(Long taskHistoryId) {
        this.taskHistoryId = taskHistoryId;
    }

    public Long getTaskHistoryId() {
        return taskHistoryId;
    }

    public void setTaskHistoryId(Long taskHistoryId) {
        this.taskHistoryId = taskHistoryId;
    }

    public Integer getTaskFactoryId() {
        return taskFactoryId;
    }

    public void setTaskFactoryId(Integer taskFactoryId) {
        this.taskFactoryId = taskFactoryId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public Double getCompletenessAtFinish() {
        return completenessAtFinish;
    }

    public void setCompletenessAtFinish(Double completenessAtFinish) {
        this.completenessAtFinish = completenessAtFinish;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TaskHistoryStatus getTaskHistoryStatusId() {
        return taskHistoryStatusId;
    }

    public void setTaskHistoryStatusId(TaskHistoryStatus taskHistoryStatusId) {
        this.taskHistoryStatusId = taskHistoryStatusId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (taskHistoryId != null ? taskHistoryId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TaskHistory)) {
            return false;
        }
        TaskHistory other = (TaskHistory) object;
        if ((this.taskHistoryId == null && other.taskHistoryId != null) || (this.taskHistoryId != null && !this.taskHistoryId.equals(other.taskHistoryId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.TaskHistory[ taskHistoryId=" + taskHistoryId + " ]";
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
