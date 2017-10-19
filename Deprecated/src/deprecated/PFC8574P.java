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


import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Washington iGEM Team 2017
 */
public class PFC8574P {
    
    public static final byte PCF8574P_ADDRESS       =  (byte)(0x20);
    
    public static void main(String[] args) 
            throws IOException, I2CFactory.UnsupportedBusNumberException, 
            InterruptedException {
        
        // Connect to I2C bus 1
        I2CBus i2c = I2CFactory.getInstance(I2CBus.BUS_1);
        
        // Create device object
        I2CDevice device = i2c.getDevice(PCF8574P_ADDRESS);
        
        while(true) {
            device.write((byte)0x40,(byte)0);
            Thread.sleep(1000);
            device.write((byte)0x40,(byte)1);
        }
    }
}
