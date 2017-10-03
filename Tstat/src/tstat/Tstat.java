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

import deprecated.MCP23017_Control;
import java.util.*;
import java.lang.*;
import com.pi4j.io.gpio.*;
import com.pi4j.util.CommandArgumentParser;
import com.pi4j.util.Console;
import java.io.IOException;
import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;


/**
 * Turbidostat control application: controls all lab equipment attached to
 * the turbidostat.
 * 
 * @author Washington iGEM Team 2017
 */
public class Tstat {
    
    // Pins for each specific part of the pump listed in order: pump 0, 1, 2
    // TODO: All these pins are placeholder pins
    public static final Pin[] DIR_PINS = {MCP23017Pin.GPIO_B7, MCP23017Pin.GPIO_B5, MCP23017Pin.GPIO_B1};
    public static final Pin[] STEP_PINS  = {MCP23017Pin.GPIO_B6, MCP23017Pin.GPIO_B4, MCP23017Pin.GPIO_B0};
    public static final Pin[] ENABLE_PINS = {MCP23017Pin.GPIO_B3, MCP23017Pin.GPIO_B2, MCP23017Pin.GPIO_A7};
    public static final Pin[] MAX_STOP_PINS = {MCP23017Pin.GPIO_B2, MCP23017Pin.GPIO_B4, MCP23017Pin.GPIO_B6};
    public static final Pin[] MIN_STOP_PINS = {MCP23017Pin.GPIO_B3, MCP23017Pin.GPIO_B5, MCP23017Pin.GPIO_B7};
    public static final double DEFAULT_RATE = 1.0;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException, UnsupportedBusNumberException, IOException{
        
        System.out.println("Turbidostat Application Run");
        System.out.println("Setting up Raspberry Pi and Syringe Pumps");
        
        final GpioController gpio = GpioFactory.getInstance();
        
        MCP23017_Control MCPControl0x20 = new MCP23017_Control(0x20);
        MCP23017_Control MCPControl0x21 = new MCP23017_Control(0x21);
        
        Fluids fluids = new Fluids();
        for(int i = 0; i < 3; i++) {
            SyringePump addPump = new SyringePump(
                    MCPControl0x20.provisionOutput(DIR_PINS[i], PinState.LOW),
                    MCPControl0x20.provisionOutput(STEP_PINS[i], PinState.LOW),
                    MCPControl0x20.provisionOutput(ENABLE_PINS[i], PinState.LOW),
                    MCPControl0x21.provisionInput(MAX_STOP_PINS[i]),
                    MCPControl0x21.provisionInput(MIN_STOP_PINS[i]),
                    DEFAULT_RATE
            );
            
            fluids.addPump(addPump);
        }
        
        /////// INSERT FLUIDS COMMANDS HERE
        
        
        System.out.println("Calibrating pumps");
        
        for(int i = 0; i < 3; i++) {
            System.out.println("Calibrating pump #" + i);
            fluids.calibrateFrom(i);
        }
        
        
    }
}
