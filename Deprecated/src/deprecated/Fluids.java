package deprecated;

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


import java.util.ArrayList;

/**
 * Controls fluid dispensation by syringe pumps
 * 
 * @author Washington iGEM Team 2017
 */
public class Fluids {
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
     * @throws java.lang.InterruptedException Uses GPIO
     */
    public void dispenseFrom(double mL, int pump) throws InterruptedException {
        pumps.get(pump).dispense(mL);
    }
    
    /**
     * Changes the step delay from the specified pump
     * @param delay Milliseconds between steps of the motor
     * @param pump Which pump to modify
     * @throws java.lang.InterruptedException Uses GPIO
     */
    public void setStepDelayFrom(int delay, int pump) throws InterruptedException {
        pumps.get(pump).setStepDelay(delay);
    }
    
    /**
     * Changes the rate of the specified pump
     * @param rate in steps per milliliter
     * @param pump Which pump to modify
     * @throws java.lang.InterruptedException Uses GPIO
     */
    public void setRateFrom(int rate, int pump) throws InterruptedException {
        pumps.get(pump).setRate(rate);
    }
    
    /**
     * Runs calibration procedure on specified pump
     * @param pump Which pump to calibrate
     * @throws InterruptedException 
     */
    public void calibrateFrom(int pump) throws InterruptedException {
        pumps.get(pump).calibrate(pump);
    }
    
    /**
     * Gets rate of motor movement from specified pump in steps per milliliter
     * @param pump Which pump to analyze
     * @return rate of motor movement from specified pump in steps per milliliter
     * @throws InterruptedException
     */
    public double getRateFrom(int pump) throws InterruptedException {
        return pumps.get(pump).getRate();
    }
    
    /**
     * Updates current amount of running steps in specified pump by specified
     * amount of steps
     * @param steps Updates current amount of steps by this amount
     * @param pump Which pump to update steps from
     * @throws InterruptedException 
     */
    public void updateStepsToTakeFrom(int steps, int pump) throws InterruptedException {
        pumps.get(pump).updateStepsToTake(steps);
    }
    
    /**
     * Returns current position of pump in steps
     * @param pump Which pump is being analyzed
     * @return current position of specified pump in steps
     * @throws InterruptedException 
     */
    public int getCurrentPositionFrom(int pump) throws InterruptedException {
        return pumps.get(pump).getCurrentPosition();
    }
    
}
