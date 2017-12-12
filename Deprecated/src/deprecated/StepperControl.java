package deprecated;

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



import java.util.Scanner;
import java.lang.*;
import com.pi4j.io.gpio.*;
import com.pi4j.util.CommandArgumentParser;
import com.pi4j.util.Console;
/**
 *
 * @author Washington iGEM Team 2017
 */
public class StepperControl {
    public static void main(String args[]) 
    throws InterruptedException {
        System.out.println("StepperControl is running...");

	// Create a GPIO controller instance
	final GpioController gpio = GpioFactory.getInstance();	
	
	// Try to create a software PWM pin output
	GpioPinDigitalOutput pin12 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_26,"pin12");
	GpioPinDigitalOutput pinDir = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06,"pinDir");
	

	pinDir.high();
	for(int i = 0; i <  500; i ++){
	    
	    /* if(i==0)
		{
		    System.out.println("Pin 12 High");
		    pin12.high();
		    i = 1;
		}
	    else
		{	
		    System.out.println("Pin 12 Low");
		    pin12.low();
		    i = 0;
		    }*/

	    Thread.sleep(1);
		pin12.high();
	    Thread.sleep(1);
	    pin12.low();
	}
        pinDir.low();

	for(int i = 0; i <  500; i ++){
	    
	    /* if(i==0)
		{
		    System.out.println("Pin 12 High");
		    pin12.high();
		    i = 1;
		}
	    else
		{	
		    System.out.println("Pin 12 Low");
		    pin12.low();
		    i = 0;
		    }*/

	    Thread.sleep(1);
		pin12.high();
	    Thread.sleep(1);
	    pin12.low();
	}
    }
}
