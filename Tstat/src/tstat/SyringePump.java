/*
 * Copyright (C) 2017 Washington iGEM Team 2017 <uwigem@uw.edu>
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

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinInput;

/**
 * 
 * @author Washington iGEM Team 2017 <uwigem@uw.edu>
 */

public class SyringePump {
    private GpioPinDigitalOutput dirPin;
    private GpioPinDigitalOutput stepPin;
    private GpioPinDigitalOutput enablePin;
    private GpioPinInput endHigh;
    private GpioPinInput endLow;
    private double rate; // Steps per milliliter
    private enum Direction {DISPENSE, REFILL}
    private int delay=1;
    private int maxPosition = 100; // Max steps away from home that can be taken
    private int currPosition = 0; // Considered "home" position
    
    /**
    * 
    * @param dirPin Pin used to choose direction.
    * @param stepPin Pin used to take steps
    * @param enablePin Pin used to DISABLE the motor
    * @param endHigh Pin that goes high when at the end
    * @param endLow Pin that goes high when at the end
    * @param rate Number of steps for 1 mL fluid moved
    */
    public SyringePump(GpioPinDigitalOutput dirPin, GpioPinDigitalOutput stepPin, GpioPinDigitalOutput enablePin, 
            GpioPinInput endHigh, GpioPinInput endLow, double rate){
        this.rate = rate;
        this.dirPin = dirPin;
        this.stepPin = stepPin;
        this.enablePin = enablePin;
        this.endHigh = endHigh;
        this.endLow = endLow;
    } // TODO: Constructor has many arguments. Can it be made shorter?
    
    /**
     * Sets delay between steps. Default delay is 1 millisecond.
     * @param delay ms between steps
     */
    public void setStepDelay(int delay) {
        if(delay > 0) {
            this.delay = delay;
        }
    }
    
    /**
     * Calibrates pump by setting number of steps to move 1mL of fluid
     * @param steps Number of steps for 1mL dispensed
     */
    public void calibrate() {
        // TODO: Add endstop calibration. 
        // This will take care of setting the max/min positions automatically.
        // max and min will be set after this point.
        
        // Dispense until endstop hit. Store location as home (0);
        this.currPosition = 0;
        
        // TODO:
        // Refill until endstop hit. Store location as max steps position
        this.maxPosition = currPosition;
    }
    
    public int dispense(double mL) throws InterruptedException {
        int steps = (int)(mL * this.rate);
        return takeSteps(steps, SyringePump.Direction.DISPENSE);        
    }
    
    /**
     * Refills the syringe completely from wherever it is now.
     * @throws InterruptedException 
     */
    public void refill() throws InterruptedException {
        takeSteps(maxPosition-currPosition, SyringePump.Direction.REFILL);        
    }
    
    /**
     * 
     * @param steps Number of steps to take
     * @param delay Delay in ms between steps
     * @return Number of steps that failed due to endstop. 0 if steps successful
     */
    private int takeSteps(int steps, SyringePump.Direction dir) 
            throws InterruptedException {
        
        int addStep;
        
        if(dir == SyringePump.Direction.DISPENSE) {
            // TODO: Set the dir pin to the correct level (lo/hi) for dispense
            addStep = 1;
        }
        else {
            // TODO: Set the dir pin to the correct level (lo/hi) for refill
            addStep = -1;
        }
        
        while(steps > 0 && this.canMove()) {
            // Take one step
            this.stepPin.high();          
            Thread.sleep(delay); // Technically shouldn't do this in loop
            this.stepPin.low();
            this.currPosition += addStep; // Update current position
            steps--;
        }
        
        // If we reached the end of the syringe, then it's empty. Refill it.
        if(currPosition == maxPosition) {
            refill();
        }
        
        return steps;
        
    }
    
    /**
     * Used to determine if the motor can be moved.
     * @return True if neither endstop is being pressed, false otherwise
     */
    private Boolean canMove() {
        // Get the value of the endstop input pin, high or low
        // Return true if neither endstop switch is being pressed
        
        // For now, always move. Be careful!
        return true;
    }
}
