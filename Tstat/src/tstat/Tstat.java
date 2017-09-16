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

import java.util.*;
import java.lang.*;
import com.pi4j.io.gpio.*;
import com.pi4j.util.CommandArgumentParser;
import com.pi4j.util.Console;



/**
 * Turbidostat control application: controls all lab equipment attached to
 * the turbidostat.
 * 
 * @author Washington iGEM Team 2017
 */
public class Tstat {
    
    // Pins for each specific part of the pump listed in order: pump 0, 1, 2
    // TODO: All these pins are placeholder pins
    public static final Pin[] DIR_PINS = {RaspiPin.GPIO_29, RaspiPin.GPIO_30, RaspiPin.GPIO_31};
    public static final Pin[] STEP_PINS  = {RaspiPin.GPIO_26, RaspiPin.GPIO_27, RaspiPin.GPIO_28};
    public static final Pin[] ENABLE_PINS = {RaspiPin.GPIO_29, RaspiPin.GPIO_30, RaspiPin.GPIO_31};
    public static final Pin[] MAX_STOP_PINS = {RaspiPin.GPIO_29, RaspiPin.GPIO_30, RaspiPin.GPIO_31};
    public static final Pin[] MIN_STOP_PINS = {RaspiPin.GPIO_29, RaspiPin.GPIO_30, RaspiPin.GPIO_31};
    public static final double DEFAULT_RATE = 1.0;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        
        System.out.println("Turbidostat Application Run");
        System.out.println("Setting up Raspberry Pi and Syringe Pumps");
        
        final GpioController gpio = GpioFactory.getInstance();
        Fluids fluids = new Fluids();
        for(int i = 0; i < 3; i++) {
            SyringePump addPump = new SyringePump(gpio.provisionDigitalOutputPin(DIR_PINS[i]), 
                                                    gpio.provisionDigitalOutputPin(STEP_PINS[i]), 
                                                    gpio.provisionDigitalOutputPin(ENABLE_PINS[i]), 
                                                    gpio.provisionDigitalInputPin(MAX_STOP_PINS[i]), 
                                                    gpio.provisionDigitalInputPin(MIN_STOP_PINS[i]), 
                                                    DEFAULT_RATE);
            fluids.addPump(addPump);
        }
        
        System.out.println("Calibrating pumps");
        
        for(int i = 0; i < 3; i++) {
            System.out.println("Calibrating pump #" + i);
            fluids.calibrateFrom(i);
        }
        
        
    }
}
