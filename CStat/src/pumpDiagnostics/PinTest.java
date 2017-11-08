/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pumpDiagnostics;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import java.io.IOException;

/**
 *
 * @author jason_000
 */
public class PinTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        final GpioController gpio = GpioFactory.getInstance();	//get the reference to pin numbers
        GpioPinDigitalOutput pinDir = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "Direction (BCM 23)", PinState.LOW);
        GpioPinDigitalOutput pinStep = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, "Step (BCM 25)", PinState.LOW);
        GpioPinDigitalOutput pinEnable = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29, "~Enable (BCM 21)", PinState.HIGH);

        while (System.in.available() == 0) {
            pinEnable.high();
            Thread.sleep(1000);
            pinEnable.low();
            Thread.sleep(1000);
        }
    }
}
