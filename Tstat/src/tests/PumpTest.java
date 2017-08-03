/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;
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
        System.out.println("<--Pi4J--> GPIO Control Example ... started.");
        
        final GpioController gpio = GpioFactory.getInstance();	
	// Try to create a software PWM pin output
	GpioPinDigitalOutput pin12 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_26,"pin12");
	GpioPinDigitalOutput pinDir = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06,"pinDir");

        Scanner input = new Scanner(System.in);
        int distance = 220;
        int speed = 1;
        while(true) {
            System.out.println("=====================================");
            System.out.println("   Current distance: " + distance);
            System.out.println("   Current thread.sleep: " + speed);
            System.out.println("   Options");
            System.out.println("   1 for fill");
            System.out.println("   2 for dispense");
            System.out.println("   3 to change sleep");
            System.out.println("   4 to change distance");
            System.out.println("=====================================");
            int inputValue = input.nextInt();
            if(inputValue == 1) {
                System.out.println("Filling");
                pinDir.high();
                for(int i = 0; i <  distance; i ++){
                    Thread.sleep(speed);
                    pin12.high();
                    Thread.sleep(speed);
                    pin12.low();
                }
            } else if (inputValue == 2) {
                System.out.println("Dispensing");
                pinDir.low();
                for(int i = 0; i <  distance; i ++){
                    Thread.sleep(speed);
                    pin12.high();
                    Thread.sleep(speed);
                    pin12.low();
                }
            } else if(inputValue == 3) {
                System.out.println("New sleep? (Current is " + speed + ")");
                speed = input.nextInt();
                System.out.println("Sleep updated to " + speed);
            } else if(inputValue == 4) {
                System.out.println("New distance? (Current is " + distance + ")");
                distance = input.nextInt();
                System.out.println("distance updated to " + distance);
            }
            else {
                pin12.low();
                pinDir.low();
                break;
            } 
        }
    }
}
