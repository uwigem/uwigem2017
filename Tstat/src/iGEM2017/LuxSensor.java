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
package iGEM2017;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import java.io.IOException;

/**
 * A class which allows readings from a LuxSensor
 lux sensor connected to the Raspberry Pi
 I2C data pins.
 * 
 * @author Washington iGEM Team 2017
 */
public class LuxSensor {

    // Verbose debug text
    public static final boolean VERBOSE = false;

    public static final byte ADDR_DEFAULT         = 0x39; 
    public static final byte ADDR_PIN_HIGH        = 0x49; 
    public static final byte ADDR_PIN_LOW         = 0x29; 
    public static final byte COMMAND_BIT          = 0x8;

    public static final byte CONTROL              = 0x0;
    public static final byte TIMING               = 0x1;
    public static final byte INTERRUPT            = 0x6;
    public static final byte CHIP_INFO            = 0xA;
    public static final byte STARTUP              = 0x3;

    // Data Registers
    public static final byte DATA_0_LOW           = 0xC; // First half of 16-bit data0
    public static final byte DATA_0_HIGH          = 0xD; // Second half of 16-bit data0

    public static final byte DATA_1_LOW           = 0xE; // First half of 16-bit data1
    public static final byte DATA_1_HIGH          = 0xF; //Second half of 16-bit data1
    public static final byte TSL2561_REG_ID       = (byte) 0x8A;
    public static final byte TCS34725_COMMAND_BIT = (byte) 0x80;
    public static final byte TSL2561_REG_CONTROL  = (byte) 0x80;

    // LuxSensor power control values
    public static final byte TSL2561_POWER_UP = (byte) 0x03;

    I2CBus i2cBus;
    I2CDevice TSL2561;
    
    public LuxSensor() throws 
            IOException, I2CFactory.UnsupportedBusNumberException, InterruptedException{
        this((byte)0x39);
    }

    /**
     * 
     * @param addr I2C hardware address of the device. For us that's 0x39
     * @throws IOException
     * @throws com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException
     * @throws InterruptedException 
     */
    public LuxSensor(byte addr)
            throws IOException, I2CFactory.UnsupportedBusNumberException, InterruptedException {
        // Get i2c i2cBus
        // Number depends on RasPI version
        i2cBus = I2CFactory.getInstance(I2CBus.BUS_1);

        // Get device itself
        TSL2561 = i2cBus.getDevice(addr);

        // Initialize device by issuing command with data value 0x03        
        TSL2561.write(TSL2561_REG_CONTROL, TSL2561_POWER_UP);

    } 

    /**
     * 
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
    public double getReading()
            throws IOException, InterruptedException {

        byte d0L = (byte) TSL2561.read((byte) (TCS34725_COMMAND_BIT | DATA_0_LOW));
        byte d0H = (byte) TSL2561.read((byte) (TCS34725_COMMAND_BIT | DATA_0_HIGH));
        byte d1L = (byte) TSL2561.read((byte) (TCS34725_COMMAND_BIT | DATA_1_LOW));
        byte d1H = (byte) TSL2561.read((byte) (TCS34725_COMMAND_BIT | DATA_1_HIGH));

        if (VERBOSE) {
            System.out.println("Data 0: " + bytesToInt(d0H, d0L));
            System.out.println("Data 1: " + bytesToInt(d1H, d1L));
        }
        
        return rawToLux(bytesToInt(d0H, d0L), bytesToInt(d1H, d1L));
    }

    /**
     * Calculates lux measurement based on both diodes
     *
     * @return Lux value for the sensor
     */
    private double rawToLux(int vis, int IR) {
        if (vis <= 0) {
            return 0;
        }
        double ratio = IR / vis;
        double exp = 0;
        double ch0Coeff = 0;
        double ch1Coeff = 0;

        if (0 < ratio && ratio < 0.5) {
            ch0Coeff = .0304;
            ch1Coeff = .062;
            exp = 1.4;
        } else if (ratio <= 0.61) {
            ch0Coeff = .0224;
            ch1Coeff = .031;
        } else if (ratio <= 0.80) {
            ch0Coeff = .0128;
            ch1Coeff = .0153;
        } else if (ratio <= 1.30) {
            ch0Coeff = .00146;
            ch1Coeff = .00112;
        }

        // If ratio > 1.3, the return value is 0.
        return ch0Coeff * vis - ch1Coeff * IR * (Math.pow(ratio, exp));
    }

    /**
     *
     * @param high Most significant byte (largest; 256+)
     * @param low Least significant byte (smallest; under 256)
     * @return Short integer having the value represented by the adjoined bytes
     */
    private static int bytesToInt(byte high, byte low) {
        return ((high & 0xFF) << 8) | (low & 0xFF);
    }
}
