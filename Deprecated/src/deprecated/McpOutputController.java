package deprecated;

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


import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Allows toggling on/off of MCP23017 pins (all are set to output) by assigning
 * an integer to each pin. All pins start off in the LOW state.
 *
 * Uses the following indexing: Index 0-7 = A0-A7 Index 8-15 =B0-B7
 *
 * @author Washington iGEM Team 2017
 */
public class McpOutputController {

    private GpioController gpioCtrl;
    private MCP23017GpioProvider provider;
    GpioPinDigitalOutput[] pins;

    public McpOutputController(int addr) {

        // Basic required setup
        this.gpioCtrl = GpioFactory.getInstance();
        try {
            this.provider = new MCP23017GpioProvider(I2CBus.BUS_1, addr);
        } catch (I2CFactory.UnsupportedBusNumberException | IOException ex) {
            Logger.getLogger(McpOutputController.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Initialize all pins to output
        this.initPins();
    }

    private void initPins() {
        this.pins = new GpioPinDigitalOutput[16];

        int k = 0;
        for (Pin p : MCP23017Pin.ALL) {
            this.pins[k] = this.pins[k] = gpioCtrl.provisionDigitalOutputPin(provider, p, "MCP " + p.getName(), PinState.LOW);
            k++;
        }
    }

    public void setPinState(int index, boolean state) {
        if (state) {
            this.setPinState(index, PinState.HIGH);
        } else {
            this.setPinState(index, PinState.LOW);
        }

    }

    public void setPinState(int index, PinState state) {
        System.out.println("Setting pin " + index + " (" + this.pins[index].getName() + ") state to " + state);
        gpioCtrl.setState(state, pins[index]);
    }
}
