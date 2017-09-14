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
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinInput;
import java.util.*;
/**
 * A single syringe pump controlled with a stepper motor
 * @author Washington iGEM Team 2017
 */
public class SyringePump {
    private GpioPinDigitalOutput dirPin;
    private GpioPinDigitalOutput stepPin;
    private GpioPinDigitalOutput enablePin;
    private GpioPinDigitalInput endHigh;
    private GpioPinDigitalInput endLow;
    private double rate; // Steps per milliliter
    private enum Direction {DISPENSE, REFILL}
    private boolean justFilled = false;
    private int delay = 1;
    private int maxPosition = 100; // Max steps away from home that can be taken
    private int currPosition = 0; // Considered "home" position
    private int stepsToTake = 0;
    private static final int HALF_CALIBRATION_STEPS = 10;
    private static final int WAIT_BETWEEN_CALIBRATION = 1000;
    
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
            GpioPinDigitalInput endHigh, GpioPinDigitalInput endLow, double rate){
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
    
    public void setRate(int steps) {
        this.rate = steps;
    }
    
    /**
     * Calibrates pump by setting number of steps to move 1mL of fluid
     * @throws InterruptedException
     */
    public void calibrate() throws InterruptedException{
        this.currPosition = 0;
        this.maxPosition = currPosition;
        Scanner input = new Scanner(System.in);
        
        double maxML = 0.0;
        int maxSteps = 0;
        
        this.dirPin.high();
        while(this.canMove()) {
            // Take one step
            this.stepPin.high();          
            Thread.sleep(delay); // Technically shouldn't do this in loop
            this.stepPin.low();
            Thread.sleep(delay);
        }
        
        System.out.println("What is the current syringe measurement in mL?");
        
        double measurementOne = input.nextDouble();
        if(measurementOne > 0.0) {
            maxML = measurementOne;
        }
        
        this.dirPin.low();
        while(this.canMove()) {
            // Take one step
            this.stepPin.high();          
            Thread.sleep(delay); // Technically shouldn't do this in loop
            this.stepPin.low();
            Thread.sleep(delay);
            maxSteps++;
        }
        
        System.out.println("What is the current syringe measurement in mL?");
        
        double measurementTwo = input.nextDouble();
        if(measurementTwo > 0.0) {
            maxML = measurementTwo;
        }
        
        // Stores rate in steps per mL
        // stores max position in steps
        int stepsPerMil = (int)(((double)maxSteps)/maxML);
        this.rate = stepsPerMil;
        this.maxPosition = maxSteps;
        
    }
    /**
     * Returns the rate for reference in PumpTest.java
     * @return rate
     */
    public double getRate() {
        return this.rate;
    }
    
    /**
     * Dispenses desired amount of mLs of fluid by converting mLs to number of 
     * steps to be taken
     * @param mL Number of desired mLs of fluid to dispense
     * @return Number of steps needed to dispense the specific amount of mLs
     * @throws InterruptedException 
     */
    public int dispense(double mL) throws InterruptedException {
        int steps = (int)(mL * this.rate);
        //take the appropriate number of steps
        //returns number of steps taken
        return takeSteps(steps, SyringePump.Direction.DISPENSE);        
    }
    
    /**
     * Refills the syringe completely from wherever it is now.
     * @throws InterruptedException Uses GPIO
     */
    public void refill() throws InterruptedException {
        takeSteps(maxPosition-currPosition, SyringePump.Direction.REFILL);        
    }
    
    /**
     * Syringe moves specified number of steps in the specified direction
     * @param steps Number of steps to take, can be updated with updateStepsToTake();
     * @param delay Delay in ms between steps
     * @return Number of steps that failed due to endstop. 0 if steps successful
     */
    private int takeSteps(int steps, SyringePump.Direction dir) 
            throws InterruptedException {
        
        int addStep; // variable representing direction change in position of syringe (pos if dispense, neg if refill)
        this.stepsToTake = steps; //This sets a global variable so that it can be modified by updateStepsToTake()
        
        // Set up the direction to move the motor
        if(dir == SyringePump.Direction.DISPENSE && this.canMove()) {
            this.checkNeedRefill();
            this.dirPin.low();
            addStep = 1;
            if(this.justFilled) {
                for(int i = 0; i <  HALF_CALIBRATION_STEPS; i++){
                    Thread.sleep(this.delay);
                    this.stepPin.high();
                    Thread.sleep(this.delay);
                    this.stepPin.low();
                    this.currPosition += addStep;
                }
                Thread.sleep(WAIT_BETWEEN_CALIBRATION);
                for(int i = 0; i < HALF_CALIBRATION_STEPS; i++){
                    Thread.sleep(this.delay);
                    this.stepPin.high();
                    Thread.sleep(this.delay);
                    this.stepPin.low();
                    this.currPosition += addStep;
                }
            }
            this.justFilled = false;
        } else {
            this.dirPin.high();
            this.justFilled = true;
            addStep = -1;
        }
        
        // loop to move the decided amount of steps
        while(this.stepsToTake > 0 && this.canMove()) {
            // Take one step
            this.stepPin.high(); 
            Thread.sleep(this.delay); // Technically shouldn't do this in loop
            this.stepPin.low();
            Thread.sleep(this.delay);
            this.currPosition += addStep; // Update current position
            this.updateStepsToTake(-1); // Use this method so that it doesn't go below zero
            if(dir == SyringePump.Direction.DISPENSE) {
                this.checkNeedRefill();
                this.dirPin.low();          // This is called because when refill() is called, dirPin is set to high.
            }
        }
        // return number of steps taken
        return steps;
    }
    
    /**
     * @param steps updates the current amount of steps by this amount 
     * This amount is additive and will do nothing if current amount of steps
     * taken + steps is less than or equal to 0
     */
    public void updateStepsToTake(int steps) {
        if(this.stepsToTake + steps >= 0) {
            this.stepsToTake += steps;
        }
    }
    
    /**
     * Checks if the position is less than or equal to 100 steps left, and if it is, then calls refill()
     * @throws InterruptedException 
     */
    private void checkNeedRefill() throws InterruptedException {
        // 100 is an arbitrary value as of now, mainly because getting too close to the end results
        // in iffy behavior by the pump.
        if(this.currPosition <= 100) {
            this.refill();
        }
    }
    
    /**
     * Used to determine if the motor can be moved.
     * @return True if neither endstop is being pressed, false otherwise
     */
    private Boolean canMove() {
        return !(endHigh.isHigh() || endLow.isHigh());
    }
}