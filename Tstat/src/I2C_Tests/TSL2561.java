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
package I2C_Tests;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Washington iGEM Team 2017
 * 
 * The TSL2561 is a light-to-digital converter that transforms light
 * intensity to an I2C signal. This code is designed to work with a
 * Raspberry Pi and the Pi4J libraries.
 * 
 */


public class TSL2561 {
    
    public enum ADDR_PIN_STATE{ GND, FLOATING, HIGH_3V3}
    
    public static final byte TSL2561_REG_CONTROL = (byte) 0x80;
    public static final byte TSL2561_POWER_UP = (byte) 0x03;
    public static final byte COMMAND_BIT = 0x8;
    public static final byte DATA_0_LOW = 0xC;  // First half of 16-bit data0
    public static final byte DATA_0_HIGH = 0xD; // Second half of 16-bit data0
    
    public static final byte DATA_1_LOW = 0xE; // First half of 16-bit data1
    public static final byte DATA_1_HIGH = 0xF; //Second half of 16-bit data1
    
    private byte device_addr;
    private I2CBus i2cBus;
    private I2CDevice device;
    
    private int lastVisibleReading;
    private int lastInfraredReading;
    
    public boolean VERBOSE = false;
    
    
    /**
     * Creates a new controller object for a TSL2561 device.
     * @param p The status of the "ADR" pin on the device. This determines
     * a unique address for the device. Two devices on the same I2C network
     * cannot share an address. The addresses are:
     * 
     * GND:      0x29
     * Floating: 0x39
     * 3V3:     0x49
     */
    public TSL2561(ADDR_PIN_STATE p){
        try {
            // Set the device address based on the ADR pin state.
            // Defaults to 0x49
            if(null == p){
                this.device_addr = 0x49;
            } else switch (p) {
                case GND:
                    this.device_addr = 0x29;
                    break;
                case FLOATING:
                    this.device_addr = 0x39;
                    break;
                default:
                    this.device_addr = 0x49;
                    break;
            }
            
            this.i2cBus = I2CFactory.getInstance(I2CBus.BUS_1);
            this.device = i2cBus.getDevice(device_addr);            
            
        } catch (I2CFactory.UnsupportedBusNumberException ex) {
            System.out.println("Error: That is not a valid bus number.\n"
                    + "Bus numbers can be dependent on model.");
            Logger.getLogger(TSL2561.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println("The bus number is valid, but there's some problem\n"
                    + "communicating with the device.");
            Logger.getLogger(TSL2561.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    /**
     * Takes a reading from the sensor and saves the information
     * @param integrationTime How many milliseconds to integrate sensor input
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    public void takeReading(int integrationTime) 
            throws IOException, InterruptedException{
            
            //device.write(TSL2561_REG_CONTROL, (byte)0x0);
            //Thread.sleep(50);
        
            device.write(TSL2561_REG_CONTROL, TSL2561_POWER_UP);

            Thread.sleep(integrationTime);
            byte d0L = (byte)device.read((byte)(COMMAND_BIT | DATA_0_LOW));
            byte d0H = (byte)device.read((byte)(COMMAND_BIT | DATA_0_HIGH));
            byte d1L = (byte)device.read((byte)(COMMAND_BIT | DATA_1_LOW)); 
            byte d1H = (byte)device.read((byte)(COMMAND_BIT | DATA_1_HIGH));

            this.lastVisibleReading = bytesToInt(d0H, d0L);
            this.lastInfraredReading = bytesToInt(d1H, d1L);

            if (VERBOSE){
                System.out.println("Visible light reading: " + this.lastVisibleReading);
                System.out.println("Infrared light reading: " + this.lastInfraredReading);
            }
    }
    
    /**
     * Get the value of the last visible light reading taken
     * @return Value of last reading as an integer
     */
    public int getLastVisible(){
        return this.lastVisibleReading;
    }
    
    /**
     * Get the value of the last infrared light reading taken
     * @return Value of last reading as an integer
     */
    public int getLastIR(){
        return this.lastInfraredReading;
    }
        
    
    /**
     * 
     * @param high Most significant byte (largest; 256+)
     * @param low Least significant byte (smallest; under 256)
     * @return Short integer having the value represented by the adjoined bytes
     */
    public static int bytesToInt(byte high, byte low){
        return ((high & 0xFF) << 8 ) | (low & 0xFF);        
    }
    
}
