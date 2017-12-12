/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pumpDiagnostics;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import java.io.IOException;

/**
 *
 * @author jason_000
 */
public class ConsolePumpTest {

    public static void main(String[] args)
            throws InterruptedException, I2CFactory.UnsupportedBusNumberException, IOException {
        final GpioController gpio = GpioFactory.getInstance();	//get the reference to pin numbers

        int s = 100;
        int delay = 1;

        /* // USE BUILT-IN GPIO PINS
        GpioPinDigitalOutput pinStep = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_27, "BCM 16 - Step", PinState.LOW);
        GpioPinDigitalOutput pinDir = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_28, "BCM 20 - Direction", PinState.HIGH);
        GpioPinDigitalOutput pinEnable = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_23, "BCM 13 - Enable", PinState.HIGH);
         */
        // USE MCP PINS
        MCP23017GpioProvider mcpProvider = new MCP23017GpioProvider(I2CBus.BUS_1, 0x20);
        MCP23017GpioProvider mcpProvider2 = new MCP23017GpioProvider(I2CBus.BUS_1, 0x21);

        // Provision direction control pins as outputs
        System.out.println("Provisioning direction pin");
        GpioPinDigitalOutput pinDir = gpio.provisionDigitalOutputPin(mcpProvider, MCP23017Pin.GPIO_A2, "Direction", PinState.LOW);

        // Provision step control pins as outputs
        System.out.println("Provisioning step pin");
        GpioPinDigitalOutput pinStep = gpio.provisionDigitalOutputPin(mcpProvider, MCP23017Pin.GPIO_A1, "Step", PinState.LOW);

        // Provision enable pins as outputs. 
        // Note that the pin state "high" means the pump is disabled;
        // the enable pump on the chip is inverted.
        System.out.println("Provisioning enable pin");
        GpioPinDigitalOutput pinEnable = gpio.provisionDigitalOutputPin(mcpProvider, MCP23017Pin.GPIO_A0, "~Enable", PinState.LOW);

        pinEnable.low();
        pinDir.high();

        for (int k = 0; k < s; k++) {
            System.out.println("Step " + k);
            pinStep.high();
            Thread.sleep(delay);
            pinStep.low();
            Thread.sleep(delay);
        }

        pinDir.low();

        for (int k = 0; k < s; k++) {
            System.out.println("Step " + (s + k));
            pinStep.high();
            Thread.sleep(delay);
            pinStep.low();
            Thread.sleep(delay);
        }

        pinEnable.high();

        // Provision direction control pins as outputs
        System.out.println("Provisioning direction pin");
        GpioPinDigitalOutput pinDir2 = gpio.provisionDigitalOutputPin(mcpProvider, MCP23017Pin.GPIO_A5, "Direction", PinState.LOW);

        // Provision step control pins as outputs
        System.out.println("Provisioning step pin");
        GpioPinDigitalOutput pinStep2 = gpio.provisionDigitalOutputPin(mcpProvider, MCP23017Pin.GPIO_A4, "Step", PinState.LOW);

        // Provision enable pins as outputs. 
        // Note that the pin state "high" means the pump is disabled;
        // the enable pump on the chip is inverted.
        System.out.println("Provisioning enable pin");
        GpioPinDigitalOutput pinEnable2 = gpio.provisionDigitalOutputPin(mcpProvider, MCP23017Pin.GPIO_A3, "~Enable", PinState.HIGH);

        pinEnable2.low();
        pinDir2.high();

        for (int k = 0; k < s; k++) {
            System.out.println("Step " + k);
            pinStep2.high();
            Thread.sleep(1);
            pinStep2.low();
            Thread.sleep(1);
        }

        pinDir2.low();

        for (int k = 0; k < s; k++) {
            System.out.println("Step " + (s - k));
            pinStep2.high();
            Thread.sleep(1);
            pinStep2.low();
            Thread.sleep(1);
        }

        pinEnable2.high();

        // Provision direction control pins as outputs
        System.out.println("Provisioning direction pin");
        GpioPinDigitalOutput pinDir3 = gpio.provisionDigitalOutputPin(mcpProvider, MCP23017Pin.GPIO_B0, "Direction", PinState.LOW);

        // Provision step control pins as outputs
        System.out.println("Provisioning step pin");
        GpioPinDigitalOutput pinStep3 = gpio.provisionDigitalOutputPin(mcpProvider, MCP23017Pin.GPIO_A7, "Step", PinState.LOW);

        // Provision enable pins as outputs. 
        // Note that the pin state "high" means the pump is disabled;
        // the enable pump on the chip is inverted.
        System.out.println("Provisioning enable pin");
        GpioPinDigitalOutput pinEnable3 = gpio.provisionDigitalOutputPin(mcpProvider, MCP23017Pin.GPIO_A6, "~Enable", PinState.HIGH);

        pinEnable3.low();
        pinDir3.high();

        for (int k = 0; k < s; k++) {
            System.out.println("Step " + k);
            pinStep3.high();
            Thread.sleep(1);
            pinStep3.low();
            Thread.sleep(1);
        }

        pinDir3.low();

        for (int k = 0; k < s; k++) {
            System.out.println("Step " + (s - k));
            pinStep3.high();
            Thread.sleep(1);
            pinStep3.low();
            Thread.sleep(1);
        }

        pinEnable3.high();

        // Test relay pins.        
        System.out.println("Testing relay control. Each relay should activate in turn.");
        GpioPinDigitalOutput[] relayPins = new GpioPinDigitalOutput[8];
        relayPins[0] = gpio.provisionDigitalOutputPin(mcpProvider2, MCP23017Pin.GPIO_B7, "R1", PinState.HIGH);
        relayPins[1] = gpio.provisionDigitalOutputPin(mcpProvider2, MCP23017Pin.GPIO_B6, "R2", PinState.HIGH);
        relayPins[2] = gpio.provisionDigitalOutputPin(mcpProvider2, MCP23017Pin.GPIO_B5, "R3", PinState.HIGH);
        relayPins[3] = gpio.provisionDigitalOutputPin(mcpProvider2, MCP23017Pin.GPIO_B4, "R4", PinState.HIGH);
        relayPins[4] = gpio.provisionDigitalOutputPin(mcpProvider2, MCP23017Pin.GPIO_B3, "R5", PinState.HIGH);
        relayPins[5] = gpio.provisionDigitalOutputPin(mcpProvider2, MCP23017Pin.GPIO_B2, "R6", PinState.HIGH);
        relayPins[6] = gpio.provisionDigitalOutputPin(mcpProvider2, MCP23017Pin.GPIO_B1, "R7", PinState.HIGH);
        relayPins[7] = gpio.provisionDigitalOutputPin(mcpProvider2, MCP23017Pin.GPIO_B0, "R8", PinState.HIGH);

        for (int k = 0; k <= 7; k++) {
            relayPins[k].low();
            Thread.sleep(1000);
            relayPins[k].high();
            Thread.sleep(1000);
        }
        
        // Test end stop sensing
        //GpioPinDigitalInput[] endStops = new GpioPinDigitalInput[6];
        //endStops[0] = gpio.provisionDigitalInputPin(mcpProvider2, MCP23017Pin, name, PinPullResistance.OFF)

    }

}
