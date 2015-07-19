/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the NIBIO Open Source License as published by 
 * NIBIO, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * NIBIO Open Source License for more details.
 * 
 * You should have received a copy of the NIBIO Open Source License
 * along with VIPSLogic.  If not, see <http://www.nibio.no/licenses/>.
 * 
 */

package no.nibio.vips.logic.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "task_history")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TaskHistory.findAll", query = "SELECT t FROM TaskHistory t"),
    @NamedQuery(name = "TaskHistory.findByTaskHistoryId", query = "SELECT t FROM TaskHistory t WHERE t.taskHistoryId = :taskHistoryId"),
    @NamedQuery(name = "TaskHistory.findByTaskFactoryId", query = "SELECT t FROM TaskHistory t WHERE t.taskFactoryId = :taskFactoryId"),
    @NamedQuery(name = "TaskHistory.findByPeriod", query = "SELECT t FROM TaskHistory t WHERE t.startTime BETWEEN :periodStart AND :periodEnd"),
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

}
