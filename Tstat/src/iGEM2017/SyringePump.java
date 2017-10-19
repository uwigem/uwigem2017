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
 * @author Washington iGEM 2017
 */
public class SyringePump {

    private GpioPinDigitalOutput pinStep, pinDir, pinEnable;
    private GpioPinDigitalInput stopMax, stopMin;
    final GpioController gpioFactory = GpioFactory.getInstance();

    private int currentPosition = -1; // -1 indicates unknown position

    // Max position == pump is maximally filled, -1 means not calibrated yet
    private int maxPosition = -1;

    // Min position == pump is maximally dispensed, -1 means not calibrated yet
    private int minPosition = -1;

    private int stepDelay = 1;    // Milliseconds between motor steps
    private double minVolume = -1; // Volume reading of syringe at max position
    private double maxVolume = -1; // Volume reading of syringe at min position

    // Motor steps per mL dispensed as calculated from calibration information
    private double stepsPerMil = -1;

    /**
     * Create a new syringe pump controller with specified I/O pins
     *
     * @param pinStep Pin used to instruct pump's motor to take one step
     * @param pinDir Pin used to choose the pump's direction
     * @param pinEnable Pin used to enable or disable the pump motor
     * @param stopMax Pin which indicates when the pump is at its max position
     * @param stopMin Pin which indicates when the pump is at its min position
     */
    public SyringePump(GpioProvider provider, Pin step, Pin direction, Pin enable, Pin max, Pin min) {

        // Provision output pins
        this.pinStep = gpioFactory.provisionDigitalOutputPin(provider, step, "step");
        this.pinDir = gpioFactory.provisionDigitalOutputPin(provider, step, "direction");
        this.pinEnable = gpioFactory.provisionDigitalOutputPin(provider, step, "enable");

        // Provision input pins
        this.stopMax = gpioFactory.provisionDigitalInputPin(provider, max, "max");
        this.stopMax = gpioFactory.provisionDigitalInputPin(provider, min, "min");
    }

    /**
     * Uses the end-stop switches to find the minimum and maximum positions of
     * the stepper motor, and also determines which direction is fill and which
     * direction is dispense.
     */
    public void calibratePosition() {
        while (stopMin.isLow()) {
            // TODO - move pump until lowest, call that 0, move to highest,
            // count steps and record as max.
        }
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
     */
    public int takeSteps(int numSteps, SyringePump.Direction direction)
            throws InterruptedException {

        int i = 0;
        for (; i < numSteps; i++) {
            this.takeStep(direction);
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
        if (direction == SyringePump.Direction.FILL && this.stopMax.isLow()) {
            return true;
        } else if (direction == SyringePump.Direction.DISPENSE && this.stopMin.isLow()) {
            return true;
        }
        
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
        Thread.sleep(this.stepDelay);
        pinStep.high();
        Thread.sleep(this.stepDelay);
        pinStep.low();
    }

    public enum Direction {
        FILL, DISPENSE
    }
}
