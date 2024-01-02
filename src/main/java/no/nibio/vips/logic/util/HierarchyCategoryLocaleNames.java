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

package no.nibio.vips.logic.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @copyright 2014 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
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
