/*
 * Copyright (C) 2017 Washington iGEM <uwigem@uw.edu>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tstat;

import java.util.ArrayList;

/**
 * Controls fluid dispensation by syringe pumps
 * 
 * @author Washington iGEM Team 2017<uwigem@uw.edu>
 */
public class Fluids {
    // TODO: "Fluids" is the thing that the main controlling application
    // will create. It needs methods like "addPump" and "dispenseFrom" etc

    private ArrayList<SyringePump> pumps;
    
    public Fluids() {
        this.pumps = new ArrayList<>();
    }
    
    /**
     * Adds another pump to the fluid control system
     * @param pump Pump to add
     */
    public void addPump(SyringePump pump) {
        this.pumps.add(pump); 
        // TODO: Client still has a pointer to pump. potential badness
    }
    
    /**
     * Dispenses fluid from the specified pump
     * @param mL Volume of fluid to dispense
     * @param pump Which pump to dispense from
     */
    public void dispenseFrom(double mL, int pump) 
            throws InterruptedException {
        pumps.get(pump).dispense(mL);
    }
    
    // Fluids does not need a refill method. Why? Because we're going to
    // have the syringepump automatically refill itself if it is empty.
    
}
