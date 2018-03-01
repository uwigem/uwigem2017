/*
 * Copyright (C) 2017 jason
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
package iGEM2017;

import com.pi4j.io.gpio.*;

/**
 * Controls one syringe pump, manages its calibration, and allows for monitoring
 * of its current status.
 *
 * @author Washington iGEM Team 2017
 */
public class SyringePump {

    private GpioPinDigitalOutput pinStep, pinDir, pinEnable;
    private GpioPinDigitalInput stopMax, stopMin;
    final GpioController gpioFactory = GpioFactory.getInstance();

    private int currentPosition = -1; // -1 indicates unknown position

    // Max position == pump is maximally filled, -1 means not calibrated yet
    private int maxPosition = -1;

    // Min position == pump is maximally dispensed. THIS IS ALWAYS ZERO.
    private final int minPosition = 0;

    private int stepDelay = 2;    // Milliseconds between motor steps
    private double minVolume = 2.5; // Volume reading of syringe at max position
    private double maxVolume = .5; // Volume reading of syringe at min position
    // private double minSyringeVolume = .5;
    // private double maxSyringeVolume = 2.5;

    // Motor steps per mL dispensed as calculated from calibration information
    private double stepsPerMil = 220;

    public SyringePump(GpioPinDigitalOutput step,
            GpioPinDigitalOutput dir, GpioPinDigitalOutput en,
            GpioPinDigitalInput min, GpioPinDigitalInput max) {

        this.pinStep = step;
        this.pinDir = dir;
        this.pinEnable = en;
        this.stopMax = max;
        this.stopMin = min;
        this.pinEnable.low(); // Enable pin is inverted. This enables the pump.
    }
    
    // Currently dispenses 2 ml's of fluid
    public void dispenseCompletely() throws InterruptedException {
        takeSteps(220, SyringePump.Direction.FILL);
    }

    // Currently fills 2 ml's of fluid
    public void fillCompletely()  throws InterruptedException {
        takeSteps(220, SyringePump.Direction.DISPENSE);
    }

    public void setMinSyringeVolume(double reading) {
        //TODO maxSyringeVolume gets set by user passed value
    }

    public void setMaxSyringeVolume(double reading) {
        // TODO maxSyringeVolume gets set by user passed value
    }

    /**
     * Sets whether or not the current minimum and maximum positions and volumes
     * should be used to calculate steps per mL
     *
     * @param b
     */
    public void setIsVolumeCalibrated(boolean b) {
        // TODO make sure the appropriate measurements have been 
        // taken. If so, then calculate steps/mL
    }

    /**
     * Stores the syringe volume measurement when pump is in max (full)
     * position.
     */
    public void setMaxVolume() {
        // TODO : Make sure pump is, in fact in the max position, then store
    }

    /**
     * Stores the syringe volume measurement when pump is in min (empty)
     * position.
     */
    public void setMinVolume() {
        // TODO : Make sure pump is, in fact in the min position, then store
    }

    /**
     * Sets speed of pump by changing delay between steps
     *
     * @param delay Time in ms between steps
     */
    public void setStepDelay(int delay) {
        this.stepDelay = delay;
    }

    /**
     * Takes a number of steps in the specified direction
     *
     * @param numSteps Number of steps to take
     * @param direction Which direction to move the pump
     * @return How many steps were actually taken. Will be less than steps
     * requested if the pump hits an end-stop.
     * @throws java.lang.InterruptedException If there is a problem when thread
     * sleeps.
     */
    public int takeSteps(int numSteps, SyringePump.Direction direction)
            throws InterruptedException {
        int i = 0;
        if (canStep(direction)) {
            for (; i < numSteps; i++) {
                this.takeStep(direction);
            }
        }
        return i;
    }

    /**
     * Checks to see if the pump can take a step in the specified direction
     *
     * @param direction Which direction to check
     * @return TRUE if and only if the pump can take a step in that direction
     */
    public boolean canStep(SyringePump.Direction direction) {

        // TODO: We don't know which direction pin state corresponds to which direction.
        // This needs to be checked against the actual hardware behavior.
        if (direction == SyringePump.Direction.FILL && this.stopMax.isHigh()) {
            return true;
        } else if (direction == SyringePump.Direction.DISPENSE && this.stopMin.isHigh()) {
            return true;
        }
        System.out.println("Cannot take a step to " + direction.name() + " because an end stop is active");
        return false;

    }

    /**
     * Instructs the motor to take one step in the positive or negative
     * direction.
     *
     * <b>WARNING:</b> This method is not hardware-safe. That is, you can
     * instruct the motor to take a step even if the pump's position is at its
     * maximum or minimum. Always check the pump's state before taking a step.
     *
     * @param positive TRUE to step in the positive direction, FALSE to step in
     * the negative direction.
     * @throws InterruptedException
     */
    private void takeStep(SyringePump.Direction direction) throws InterruptedException {

        // TODO: We don't actually know which pin state (high/low) corresponds
        // with fill and dispense yet. This could be backward.
        // Select the direction
        if (direction == SyringePump.Direction.FILL) {
            this.pinDir.high();
            this.currentPosition++;
        } else {
            this.pinDir.low();
            this.currentPosition--;
        }

        // Take one step
        System.out.println("Stepping");
        Thread.sleep(this.stepDelay);
        pinStep.high();
        Thread.sleep(this.stepDelay);
        pinStep.low();
        //Thread.sleep(this.stepDelay);
        //pinStep.high();
        //Thread.sleep(this.stepDelay);
        //pinStep.low();
    }

    /**
     * Dispenses the specified volume in mL from the pump. Note that if the pump
     * isn't calibrated completely, this will do nothing. If the pump needs to
     * refill in order to fulfill the requested volume, it will.
     *
     * @param mL Volume in milliliters to dispense
     */
    public void dispenseVolume(double mL) {
        // TODO: dispense specified volume if possible. Refill if necessary
        // to meet requested amount

        // Check isFullyCalibrated() first
    }

    /**
     * Reports whether all calibrations are complete for this pump
     *
     * @return TRUE if and only if the pump's calibrations are complete
     */
    private boolean isFullyCalibrated() {
        // Set step per mL based on current calibration
        this.stepsPerMil = ((double) this.maxPosition) / (this.maxVolume - this.minVolume);

        // Max position must be established
        // Min and max volume must be established
        // Current location must be established (relies on having max position)
        return (this.maxVolume != -1
                && this.minVolume != -1
                && this.currentPosition != -1);
                // && this.minSyringeVolume != -1
                // && this.maxSyringeVolume != -1);

    }

    /**
     * Get the current fill level of the syringe. Requires calibration.
     *
     * @return Current volume of syringe in mL.
     */
    public double currentVolume() {
        // TODO
        return 0;
    }

    public enum Direction {
        FILL, DISPENSE
    }

    public void stopReport() {
        System.out.println("Max Stop: " + this.stopMax.isHigh());
        System.out.println("Min Stop: " + this.stopMin.isHigh());
    }
}
