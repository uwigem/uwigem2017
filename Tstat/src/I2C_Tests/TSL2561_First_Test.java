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
import static deprecated.TCS34725.TCS34725_ADDRESS;
import static deprecated.TCS34725.TCS34725_COMMAND_BIT;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Washington iGEM Team 2017
 */
public class TSL2561_First_Test {

    // Verbose debug text
    public static final boolean VERBOSE = true;

    public static final byte TSL2561_ADDR = (byte) 0x39;
    public static final byte COMMAND_BIT = 0x8;

    public static final byte CONTROL = 0x0;
    public static final byte TIMING = 0x1;
    public static final byte INTERRUPT = 0x6;
    public static final byte CHIP_INFO = 0xA;
    public static final byte STARTUP = 0x3;

    // Interrupt threshold registers
    public static final byte THRESHOLD_LOW_LOWBYTE = 0x2; // Interrupt threshold
    public static final byte THRESHOLD_LOW_HIGHBYTE = 0x2;
    public static final byte THRESHOLD_HIGH_LOWBYTE = 0x2;
    public static final byte THRESHOLD_HIGH_HIGHBYTE = 0x2;

    // Data Registers
    public static final byte DATA_0_LOW = 0xC;  // First half of 16-bit data0
    public static final byte DATA_0_HIGH = 0xD; // Second half of 16-bit data0

    public static final byte DATA_1_LOW = 0xE; // First half of 16-bit data1
    public static final byte DATA_1_HIGH = 0xF; //Second half of 16-bit data1
    public static final byte TSL2561_REG_ID = (byte) 0x8A;

    public static final byte TSL2561_REG_CONTROL = (byte) 0x80;

    // TSL2561 power control values
    public static final byte TSL2561_POWER_UP = (byte) 0x03;

    // Default control values
    /**
     * Alter these values to change device operation
     *
     * @param args
     * @throws java.io.IOException
     * @throws com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException
     * @throws java.lang.InterruptedException
     */
    // Device addresses 0x29, 0x39, 0x49 are possible by setting addr pin to
    // GND, Floating, and +3.3V respectively
    public static void main(String[] args)
            throws IOException, I2CFactory.UnsupportedBusNumberException, InterruptedException {
        // Set up the I2C libraries
        I2CBus I2C_BUS;
        I2CDevice TSL2561;

        // Get i2c I2C_BUS
        // Number depends on RasPI version
        I2C_BUS = I2CFactory.getInstance(I2CBus.BUS_1);

        System.out.println("Connected to bus. OK.");

        // Get device itself
        TSL2561 = I2C_BUS.getDevice(TSL2561_ADDR);
        System.out.println("Connected to device.");

        System.out.println("Device ID: " + TSL2561.read(TSL2561_REG_ID));

        // Initialize device by issuing command with data value 0x03        
        TSL2561.write(TSL2561_REG_CONTROL, TSL2561_POWER_UP);

        while (true) {
            System.out.println("Waiting...");
            Thread.sleep(500);

            byte d0L = (byte) TSL2561.read((byte) (TCS34725_COMMAND_BIT | DATA_0_LOW));
            byte d0H = (byte) TSL2561.read((byte) (TCS34725_COMMAND_BIT | DATA_0_HIGH));
            byte d1L = (byte) TSL2561.read((byte) (TCS34725_COMMAND_BIT | DATA_1_LOW));
            byte d1H = (byte) TSL2561.read((byte) (TCS34725_COMMAND_BIT | DATA_1_HIGH));

            if (VERBOSE) {
                System.out.println("Data 0: " + bytesToInt(d0H, d0L));
                System.out.println("Data 1: " + bytesToInt(d1H, d1L));
            }
            // Output data value is expressed as a 16-bit value
            // spread across two registers

            // There are two data channels, because there are two sensors
            // in the device, each with a different spectral sensitivity curve
        }
    }

    public static int readByte(I2CDevice device, byte addr)
            throws IOException {
        return device.read(addr);
    }

    public static void writeByte(I2CDevice device, byte addr, byte data)
            throws IOException {
        device.write((int) addr, data);
    }

    /**
     * Converts a short integer into two bytes
     *
     * @param s
     * @return An array of exactly 2 bytes representing the number, with the
     * most significant bytes in index [0] and the least significant bytes in
     * index [1]
     */
    public static byte[] shortToBytes(short s) {
        byte result[] = new byte[2];
        result[1] = (byte) (s & 0xff); // Least significant 8 bits
        result[0] = (byte) ((s >> 8) & 0xff); // Most significant 8 bits
        return result;
    }

    /**
     *
     * @param high Most significant byte (largest; 256+)
     * @param low Least significant byte (smallest; under 256)
     * @return Short integer having the value represented by the adjoined bytes
     */
    public static int bytesToInt(byte high, byte low) {
        return ((high & 0xFF) << 8) | (low & 0xFF);
    }
}
