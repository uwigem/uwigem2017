package training;

import java.util.Scanner;
import java.lang.*;
import com.pi4j.io.gpio.*;
import com.pi4j.util.CommandArgumentParser;
import com.pi4j.util.Console;

public class StepperControl {
    public static void main(String args[]) 
    throws InterruptedException {
        System.out.println("Hello world! This is AcaiBerry!");

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
