/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
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

package no.bioforsk.vips.logic.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @copyright 2014 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class HierarchyCategoryLocaleNames {

    private Map<Integer, String> nameMap;
    
    public String getName(Integer hierarchyCategoryId)
    {
        return this.nameMap.get(hierarchyCategoryId);
    }
    
    public List<Integer> getHierarchyCategoryIds()
    {
        List<Integer> retVal = new ArrayList(this.getNameMap().keySet());
        Collections.sort(retVal);
        return retVal;
    }
    
    /**
     * @return the nameMap
     */
    public Map<Integer, String> getNameMap() {
        return nameMap;
    }

    /**
     * @param nameMap the nameMap to set
     */
    public void setNameMap(Map<Integer, String> nameMap) {
        this.nameMap = nameMap;
    }
    
    
}
