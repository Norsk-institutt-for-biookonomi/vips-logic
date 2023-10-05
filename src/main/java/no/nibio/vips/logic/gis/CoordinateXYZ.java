/*
 * Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.gis;

import org.locationtech.jts.geom.Coordinate;

/**
 * @copyright 2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class CoordinateXYZ extends Coordinate{
    
   /**
   *  Constructs a <code>Coordinate</code> at (x,y,z).
   *
   *@param  x  the x-ordinate
   *@param  y  the y-ordinate
   *@param  z  the z-ordinate
   */
  public CoordinateXYZ(double x, double y, double z) {
    super(x,y,z);
  }

   /**
   *  Returns <code>true</code> if <code>other</code> has the same values for
   *  the x and y ordinates.
   *  Since Coordinates are 2.5D, this routine ignores the z value when making the comparison.
   *
   *@param  other  a <code>Coordinate</code> with which to do the comparison.
   *@return        <code>true</code> if <code>other</code> is a <code>Coordinate</code>
   *      with the same values for the x and y ordinates.
   */
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Coordinate)) {
      return false;
    }
    return equals3D((Coordinate) other);
  }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }
}
