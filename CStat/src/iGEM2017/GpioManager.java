/*
 * Copyright (C) 2017 Washington iGEM Team 2017 
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

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;

/**
 * A class which manages the GPIO pins for the Raspberry Pi by assigning them as
 * requested.
 *
 * Pins can only be assigned once, and can never be reassigned.
 *
 *
 *
 * @author Washington iGEM Team 2017
 */

/*
 * For reference: These are the BCM numbers for the Raspberry Pi
 * pins that are available for assignment, and their Pi4J 
 * equivalents:
 * 
 * Index | BCM # | Pi4J #
 *  1    | 4     | 07
 *  2    | 5     | 20
 *  3    | 6     | 22
 *  4    | 12    | 26
 *  5    | 13    | 23
 *  6    | 16    | 27
 *  7    | 17    | 00
 *  8    | 19    | 24
 *  9    | 21    | 29
 *  10   | 20    | 28
 *  11   | 22    | 03
 *  12   | 23    | 04
 *  13   | 24    | 05
 *  14   | 25    | 06
 *  15   | 26    | 25
 *  16   | 27    | 02

 */
/**
 * Creates a new GpioManager object that can assign the default 16 true GPIO
 * pins from the Raspberry Pi
 *
 * @author Washington iGEM Team 2017
 */
public class GpioManager {

    // Get the singleton instance of GPIO factory
    private final GpioController gpio;

    private ArrayList<NamedPin> availablePins;
    private ArrayList<NamedPin> assignedPins;

    public GpioManager() {
        this.gpio = GpioFactory.getInstance();
        this.assignedPins = new ArrayList<NamedPin>();
        this.availablePins = new ArrayList<NamedPin>();

        // The following must be done in this long form 
        // due to the lack of pattern in true GPIO
        // pin numbering.
        availablePins.add(new NamedPin("GPIO 4", RaspiPin.GPIO_07, null));
        availablePins.add(new NamedPin("GPIO 5", RaspiPin.GPIO_20, null));
        availablePins.add(new NamedPin("GPIO 6", RaspiPin.GPIO_22, null));
        availablePins.add(new NamedPin("GPIO 12", RaspiPin.GPIO_26, null));
        availablePins.add(new NamedPin("GPIO 13", RaspiPin.GPIO_23, null));
        availablePins.add(new NamedPin("GPIO 16", RaspiPin.GPIO_27, null));
        availablePins.add(new NamedPin("GPIO 17", RaspiPin.GPIO_00, null));
        availablePins.add(new NamedPin("GPIO 19", RaspiPin.GPIO_24, null));
        availablePins.add(new NamedPin("GPIO 21", RaspiPin.GPIO_29, null));
        availablePins.add(new NamedPin("GPIO 20", RaspiPin.GPIO_28, null));
        availablePins.add(new NamedPin("GPIO 22", RaspiPin.GPIO_03, null));
        availablePins.add(new NamedPin("GPIO 23", RaspiPin.GPIO_04, null));
        availablePins.add(new NamedPin("GPIO 24", RaspiPin.GPIO_05, null));
        availablePins.add(new NamedPin("GPIO 25", RaspiPin.GPIO_06, null));
        availablePins.add(new NamedPin("GPIO 26", RaspiPin.GPIO_25, null));
        availablePins.add(new NamedPin("GPIO 27", RaspiPin.GPIO_02, null));
    }

    /**
     * Add a new MCP23017 GPIO expander chip to the manager. This allows the
     * assignment of 16 additional GPIO pins.
     *
     * Note that each chip assigned must have a different address.
     *
     * @param address The I2C address of the chip, in the range 0x20 to 0x27
     * @throws I2CFactory.UnsupportedBusNumberException If the bus # is invalid.
     * @throws IOException If the chip can't be communicated with.
     */
    public void addMcp(int bus, int address)
            throws I2CFactory.UnsupportedBusNumberException, IOException {
        GpioProvider provider = new MCP23017GpioProvider(bus, address);
        for (Pin mcpPin : MCP23017Pin.ALL) {
            String name = mcpPin.getName() + " " + Integer.toHexString(address);
            NamedPin newPin = new NamedPin(name, mcpPin, provider);
            this.availablePins.add(newPin);
        }

    }

    public int pinsAvailable() {
        return this.availablePins.size();
    }
    
    public int pinsAssigned(){
        return this.assignedPins.size();
    }

    public boolean hasNext() {
        return (!this.availablePins.isEmpty());
    }

    public void listAvailablePins() {
        for (NamedPin pin : this.availablePins) {
            System.out.println(pin.pin.getName() + " : " + pin.name);
        }
    }

    public void listAssignedPins() {
        for (NamedPin pin : this.assignedPins) {
            System.out.println(pin.pin.getName() + " : " + pin.name);
        }
    }

    public GpioPinDigitalOutput getNextOutput() {
        // No pins are left
        if (this.availablePins.isEmpty()) {
            return null;
        }

        NamedPin temp = this.availablePins.get(0);
        this.assignedPins.add(temp);
        this.availablePins.remove(temp);
        
        GpioPinDigitalOutput result;
        if(temp.provider != null){
        result = this.gpio.provisionDigitalOutputPin(temp.provider, temp.pin, PinState.LOW);
        } else {
            result = this.gpio.provisionDigitalOutputPin(temp.pin, PinState.LOW);
        }
        return result;
    }

    public GpioPinDigitalInput getNextInput(PinPullResistance ppr) {
        // No pins are left
        if (this.availablePins.isEmpty()) {
            return null;
        }

        NamedPin temp = this.availablePins.get(0);
        this.assignedPins.add(temp);
        this.availablePins.remove(temp);

        GpioPinDigitalInput result = this.gpio.provisionDigitalInputPin(temp.provider, temp.pin, temp.name, ppr);
        return result;
    }

    protected class NamedPin {

        public String name;
        public Pin pin;
        public GpioProvider provider;

        public NamedPin(String n, Pin p, GpioProvider r) {
            this.name = n;
            this.pin = p;
            this.provider = r;
        }

        @Override
        public boolean equals(Object other) {
            if (other == null) {
                return false;
            }

            if (this.provider == null && ((NamedPin) other).provider != null) {
                return false;
            } else if (this.provider != null && ((NamedPin) other).provider == null) {
                return false;
            } else if (this.provider != null && ((NamedPin) other).provider != null) {
                // Compare providers as well as other features
                return (this.name.equals(((NamedPin) other).name))
                        && (this.pin.equals(((NamedPin) other).pin))
                        && (this.provider.equals(((NamedPin) other).provider));

            } else {
                // No providers - compare only the other features of the objects
                return (this.name.equals(((NamedPin) other).name))
                        && (this.pin.equals(((NamedPin) other).pin));
            }
        }
    }

}
