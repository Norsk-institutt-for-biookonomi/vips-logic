/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
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
import java.math.BigInteger;
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
import jakarta.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "preparation")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Preparation.findAll", query = "SELECT p FROM Preparation p"),
    @NamedQuery(name = "Preparation.findByPreparationId", query = "SELECT p FROM Preparation p WHERE p.preparationId = :preparationId"),
    @NamedQuery(name = "Preparation.findByPreparationName", query = "SELECT p FROM Preparation p WHERE p.preparationName = :preparationName"),
    @NamedQuery(name = "Preparation.findByPrice", query = "SELECT p FROM Preparation p WHERE p.price = :price")})
public class Preparation implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "preparation_id")
    private Integer preparationId;
    @Size(max = 255)
    @Column(name = "preparation_name")
    private String preparationName;
    @Column(name = "price")
    private BigInteger price;
    @JoinColumn(name = "preparation_type_id", referencedColumnName = "preparation_type_id")
    @ManyToOne
    private PreparationType preparationTypeId;
    @JoinColumn(name = "currency_id", referencedColumnName = "currency_id")
    @ManyToOne
    private Currency currencyId;

    public Preparation() {
    }

    public Preparation(Integer preparationId) {
        this.preparationId = preparationId;
    }

    public Integer getPreparationId() {
        return preparationId;
    }

    public void setPreparationId(Integer preparationId) {
        this.preparationId = preparationId;
    }

    public String getPreparationName() {
        return preparationName;
    }

    public void setPreparationName(String preparationName) {
        this.preparationName = preparationName;
    }

    public BigInteger getPrice() {
        return price;
    }

    public void setPrice(BigInteger price) {
        this.price = price;
    }

    public PreparationType getPreparationTypeId() {
        return preparationTypeId;
    }

    public void setPreparationTypeId(PreparationType preparationTypeId) {
        this.preparationTypeId = preparationTypeId;
    }

    public Currency getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Currency currencyId) {
        this.currencyId = currencyId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (preparationId != null ? preparationId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Preparation)) {
            return false;
        }
        Preparation other = (Preparation) object;
        if ((this.preparationId == null && other.preparationId != null) || (this.preparationId != null && !this.preparationId.equals(other.preparationId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.Preparation[ preparationId=" + preparationId + " ]";
    }

}
