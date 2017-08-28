/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;
import tstat.SyringePump;
import java.util.Scanner;
import java.lang.*;
import com.pi4j.io.gpio.*;
import com.pi4j.util.CommandArgumentParser;
import com.pi4j.util.Console;

/**
 *
 * @author William Kwok
 * 
 * This file is a very rough test file and not optimized at all
 * 
 */
public class PumpTest {
    

    
    public static void main(String[] args) throws InterruptedException {
        // TODO code application logic here
        System.out.println("<--Pi4J--> GPIO Control Example 1.0 ... started.");
        
        
        final GpioController gpio = GpioFactory.getInstance();	
	// Try to create a software PWM pin output
	GpioPinDigitalOutput pinStep = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_26,"pinStep");
	GpioPinDigitalOutput pinDir = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06,"pinDir");
        
        GpioPinDigitalInput maxStop = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04,"maxStop");
        GpioPinDigitalInput minStop = gpio.provisionDigitalInputPin(RaspiPin.GPIO_01,"minStop");
        
        GpioPinDigitalOutput enable = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_27, "enable");

        Scanner input = new Scanner(System.in);
        
        int currentPosition = -1;
        int maxPosition = -1;
        int distance = 220;
        int speed = 1;
        int lastDirection = 0;
        int halfCalibrationSteps = 10;
        int waitBetweenCalibration = 1000;
        
        SyringePump pump = new SyringePump(pinDir, pinStep, enable, maxStop, minStop, 1.0);
        
       // pump.calibrate();
        
        
        
        
        while(true) {
            System.out.println("=====================================");
            if (currentPosition == -1) {
                System.out.println("   Current position: unknown" );
            } else {
                System.out.println("   Current position: " + currentPosition);
            }
            System.out.println("   Current distance: " + distance);
            System.out.println("   Current rate: " + pump.getRate());
            System.out.println("   Current number of ms between steps: " + speed);
            System.out.println("   Options");
            System.out.println("   1 to fill " + distance + " steps");
            System.out.println("   2 to dispense " + distance + " steps");
            System.out.println("   3 to change milliseconds between steps");
            System.out.println("   4 to change distance");
            System.out.println("   5 to calibrate min/max");
            System.out.println("   6 to change halfCalibrationSteps");
            System.out.println("   7 to change waitBetweenCalibration");
            System.out.println("=====================================");
            int inputValue = input.nextInt();
            if(inputValue == 1) {
                System.out.println("Filling");
                if(lastDirection != 1 || lastDirection != 0) {
                    pinDir.low();
                    for(int i = 0; i <  halfCalibrationSteps; i++){
                        Thread.sleep(speed);
                        pinStep.high();
                        Thread.sleep(speed);
                        pinStep.low();
                        currentPosition--;
                    }
                    Thread.sleep(waitBetweenCalibration);
                    for(int i = 0; i <  halfCalibrationSteps; i++){
                        Thread.sleep(speed);
                        pinStep.high();
                        Thread.sleep(speed);
                        pinStep.low();
                        currentPosition--;
                    }
                    lastDirection = 1;
                }
                pinDir.high();
                for(int i = 0; i <  distance; i ++){
                    Thread.sleep(speed);
                    pinStep.high();
                    Thread.sleep(speed);
                    pinStep.low();
                    currentPosition++;
                }
                reportPosition(currentPosition);
            } else if (inputValue == 2) {
                System.out.println("Dispensing");
                //System.out.println("1 to update steps");
                //int newInputValue = input.nextInt();                      
                
                if(lastDirection != 2 || lastDirection != 0) {
                    pinDir.high();
                    for(int i = 0; i <  halfCalibrationSteps; i++){
                        Thread.sleep(speed);
                        pinStep.high();
                        Thread.sleep(speed);
                        pinStep.low();
                        currentPosition++;
                    }
                    Thread.sleep(waitBetweenCalibration);
                    for(int i = 0; i <  halfCalibrationSteps; i++){
                        Thread.sleep(speed);
                        pinStep.high();
                        Thread.sleep(speed);
                        pinStep.low();
                        currentPosition++;
                    }
                    lastDirection = 2;
                }
                pinDir.low();
                int startPosition = currentPosition;
                for(int i = 0; i <  distance; i ++){
                    Thread.sleep(speed);
                    pinStep.high();
                    Thread.sleep(speed);
                    pinStep.low();
                    currentPosition--;
                }  
                /*
                if (newInputValue == 1) {
                    System.out.println("New value? (Current is " + distance + ")");
                    int newDistance = input.nextInt();
                    int distanceDifference = distance - newDistance;
                    int distanceRemaining = startPosition - currentPosition;
                    int newDistanceToFill = distanceRemaining - distanceDifference;
                    for (int i = 0; i < newDistanceToFill; i++) {
                        Thread.sleep(speed);
                        pinStep.high();
                        Thread.sleep(speed);
                        pinStep.low();
                        currentPosition--;
                    }
                }*/
                reportPosition(currentPosition);
            } else if(inputValue == 3) {
                System.out.println("New sleep? (Current is " + speed + ")");
                speed = input.nextInt();
                System.out.println("Sleep updated to " + speed);
            } else if(inputValue == 4) {
                System.out.println("New distance? (Current is " + distance + ")");
                distance = input.nextInt();
                System.out.println("distance updated to " + distance);
            } else if(inputValue == 5) {
                
                // Find the low end-stop by dispensing until it is reached
                pinDir.low();
                while(minStop.isLow())
                {
                    Thread.sleep(speed);
                    pinStep.high();
                    Thread.sleep(speed);
                    pinStep.low();
                }
                
                System.out.println("Minimum position found");
                currentPosition = 0;
                Thread.sleep(500); // Just a little pause between
                
                // Find the max end-stop by filling until it is reached
                pinDir.high();
                while(maxStop.isLow())
                {
                    Thread.sleep(speed);
                    pinStep.high();
                    Thread.sleep(speed);
                    pinStep.low();
                    currentPosition++;
                }
                System.out.println("Maximum position found");
                
                // Record the maximum position
                maxPosition = currentPosition;
                System.out.println("Maximum position =  " + maxPosition);
                
            } else if(inputValue == 6) {
                System.out.println("New halfCalibrationSteps? (Current is " + halfCalibrationSteps + ")");
                halfCalibrationSteps = input.nextInt();
                System.out.println("halfCalibrationSteps updated to " + halfCalibrationSteps);
            } else if(inputValue == 7) {
                System.out.println("New waitBetweenCalibration? (Current is " + waitBetweenCalibration + ")");
                waitBetweenCalibration = input.nextInt();
                System.out.println("halfCalibrationSteps updated to " + waitBetweenCalibration);
            }
            
            
            else {
                pinStep.low();
                pinDir.low();
                break;
            } 
        }
    }
    
    public static void reportPosition(int pos){
        System.out.println("Current position =  " + pos);
    }
}