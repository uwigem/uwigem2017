/*
 * Copyright (C) 2017 WilliamKwok
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
package PWM_Tests;

import com.pi4j.io.gpio.*;

/**
 *
 * @author WilliamKwok
 */
public class rgbControl {
    private final GpioController gpio;
    private final GpioPinPwmOutput[] pwmPins;
    
    public rgbControl(Pin red, Pin green, Pin blue) throws InterruptedException {
        this.gpio = GpioFactory.getInstance();
        this.pwmPins = new GpioPinPwmOutput[] {gpio.provisionPwmOutputPin(red), 
                    gpio.provisionPwmOutputPin(green),
                    gpio.provisionPwmOutputPin(blue)};
    }
    
    /**
     * Sets specified pin's pulse rate
     * @param pin (0 for red, 1 for green, 2 for blue)
     * @param rate rate of pin pulse in ms
     */
    public void setPinPwm(int pin, int rate) {
        pwmPins[pin].setPwm(rate);
    }
    
    /**
     * Sets color
     * @param red
     * @param green
     * @param blue 
     * TODO: Make it actually work
     */
    public void setColor(int red, int green, int blue) {
        this.setPinPwm(0, red);
        this.setPinPwm(1, green);
        this.setPinPwm(2, blue);
    }
}
