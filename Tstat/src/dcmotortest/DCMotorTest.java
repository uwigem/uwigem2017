/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dcmotortest;

import com.pi4j.io.gpio.*;
import java.io.IOException;

/**
 *
 * @author Washington iGEM Team 2017
 */
public class DCMotorTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        	// Create a GPIO controller instance
	final GpioController gpio = GpioFactory.getInstance();	
	
	// Configure GPIO pins for output	
	GpioPinDigitalOutput ledPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_22,"stepPin",PinState.HIGH);

	
	while(System.in.available() == 0) {
	    //System.out.print("Off...");
	    ledPin.low();
	    Thread.sleep(1);
	    //System.out.print("On...");
	    ledPin.high();
	    Thread.sleep(1);
	}
    }
    
}
