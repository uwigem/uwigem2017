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


import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import java.io.IOException;

/**
 *
 * @author Washington iGEM Team 2017
 */
public class TCS3200 {
    public static void main(String[] args) 
            throws IOException, InterruptedException {
        
        // Create a GPIO controller instance
	final GpioController gpio = GpioFactory.getInstance();	
        
        // Pi4J 22 = 6
        final GpioPinDigitalInput inputPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_22, PinPullResistance.PULL_DOWN);
        PinState inputState = inputPin.getState();
        /*
        while(System.in.available() == 0) {
            System.out.println(inputPin.isHigh());
            inputState = inputPin.getState();
            System.out.println(inputState.toString());
            Thread.sleep(1000);
        }*/
        
        System.out.println("Pulled down pin 22/6");

        int samples = 100000;
        
        // Take a bunch of samples and report
        while(System.in.available() == 0) {
            double sample = 0;
            for(int k = 0; k < samples ; k++) {
                if(inputPin.isHigh()) {
                    sample++;
                }
            }
            
            System.out.println(sample/samples);
        }
        
        System.out.println("Done");
    }
}
