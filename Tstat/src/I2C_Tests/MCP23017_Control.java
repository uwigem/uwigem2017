/*
 * Copyright (C) 2017 WilliamKwok
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
package I2C_Tests;

/**
 *
 * @author Washington iGEM Team 2017
 */

import java.io.IOException;
import java.util.*;
import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.*;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;


public class MCP23017_Control {
    private GpioController gpio;
    private MCP23017GpioProvider provider;
    
    public MCP23017_Control() throws UnsupportedBusNumberException, IOException {
        this.gpio = GpioFactory.getInstance();
        this.provider = new MCP23017GpioProvider(I2CBus.BUS_1, 0x21);
    }
    
    public GpioPinDigitalInput provisionInput(Pin pin) {
        return gpio.provisionDigitalInputPin(provider, pin, PinPullResistance.PULL_UP);
    }
    
    public GpioPinDigitalOutput provisionOutput(Pin pin, PinState pinState){
        return gpio.provisionDigitalOutputPin(provider, pin, pinState);
    }

}
