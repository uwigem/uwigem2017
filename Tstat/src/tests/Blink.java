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

package tests;

import java.util.Scanner;
import java.lang.*;
import com.pi4j.io.gpio.*;
import com.pi4j.util.CommandArgumentParser;
import com.pi4j.util.Console;


public class Blink {
    public static void main(String args[]) 
    throws Exception {
        System.out.println("Blink program is running. Press a key to terminate.");

	// Create a GPIO controller instance
	final GpioController gpio = GpioFactory.getInstance();	
	
	// Configure GPIO pins for output	
	GpioPinDigitalOutput ledPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_22,"dirPin",PinState.HIGH);

	
	while(System.in.available() == 0) {
	    System.out.print("Off...");
	    ledPin.low();
	    Thread.sleep(1000);
	    System.out.print("On...");
	    ledPin.high();
	    Thread.sleep(1000);
	}

	System.out.println("\n\nBlink program terminated.");
	
    }
}
