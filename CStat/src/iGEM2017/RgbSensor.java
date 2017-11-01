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
import java.awt.Color;
import java.io.IOException;

/**
 * Configures, controls, and takes readings from
 * a single Taos TCS34725 RGB (color) sensor
 * 
 * Note that this sensor's I2C address is fixed
 * at 0x29 in the hardware and cannot be changed.
 * 
 * @author Washington iGEM Team 2017
 */
public class RgbSensor {

    // Command/settings Registers
    public final static byte COMMAND_BIT = (byte) 0x80;
    public final static byte ADDR_DEFAULT = 0x29;
    public final static int TCS34725_ENABLE = 0x00;
    public final static int TCS34725_ENABLE_PON = 0x01; // Power on/off
    public final static int TCS34725_ENABLE_AEN = 0x02; // RGBC Enable
    public final static int TCS34725_ATIME = 0x01; // Integration time
    public final static int TCS34725_CONTROL = 0x0F; // Gain level 

    // Data Registers
    public final static int TCS34725_CDATAL = 0x14; // Clear channel data
    public final static int TCS34725_CDATAH = 0x15;
    public final static int TCS34725_RDATAL = 0x16; // Red channel data
    public final static int TCS34725_RDATAH = 0x17;
    public final static int TCS34725_GDATAL = 0x18; // Green channel data
    public final static int TCS34725_GDATAH = 0x19;
    public final static int TCS34725_BDATAL = 0x1A; // Blue channel data
    public final static int TCS34725_BDATAH = 0x1B;

    private final I2CDevice device;
    private final I2CBus i2cBus;
    
    // Scaling multipliers for each channel, configured by setting a white point
    private double mRed;
    private double mGreen;
    private double mBlue;
    private double mClear;

    public RgbSensor()
            throws IOException, I2CFactory.UnsupportedBusNumberException {

        // Get the I2C Bus 
        this.i2cBus = I2CFactory.getInstance(I2CBus.BUS_1);

        // Get device
        this.device = i2cBus.getDevice(ADDR_DEFAULT);

        // Initialize device
        this.readU8(0x44);

        // Enable device
        this.write8(TCS34725_ENABLE, (byte) TCS34725_ENABLE_PON);
        waitfor(10);
        this.write8(TCS34725_ENABLE, (byte) (TCS34725_ENABLE_PON | TCS34725_ENABLE_AEN));
    }

    public ColorReading getReading() throws Exception {
        int r = this.readU16(TCS34725_RDATAL);
        int b = this.readU16(TCS34725_BDATAL);
        int g = this.readU16(TCS34725_GDATAL);
        int c = this.readU16(TCS34725_CDATAL);

        return new ColorReading(r, b, g, c);
    }

    /**
     * Returns a color reading which takes into account the offset
     * created by setting a white point. Behavior undefined if white point
     * has not been set.
     * @return
     * @throws Exception 
     */
    public ColorReading getNormalizedReading() throws Exception {
        ColorReading result = getReading();

        result.red = (int) ((double) result.red * mRed);
        result.green = (int) ((double) result.green * mGreen);
        result.blue = (int) ((double) result.blue * mBlue);
        result.clear = (int) ((double) result.clear * mClear);

        return result;
    }
    
    /**
     * Extracts a hue from a color reading.
     * @param cr Color reading to convert
     * @return A color with only hue information remaining.
     */
    public Color readingToHue(ColorReading cr){
        double mult = (double)max(cr.blue,cr.green,cr.red);
        mult = 255.0 / mult;
        
        int red = (int)(cr.red * mult);
        int green = (int)(cr.green * mult);
        int blue = (int)(cr.blue * mult);
        
        return new Color(red, green, blue);

    }

    /**
     * Takes a reading and sets that to the color point 255,255,255. Raw
     * readings will not be altered, but normalized readings will be scaled
     * assuming the current white point.
     * @throws Exception If hardware failure during reading process
     */
    public void setWhitePoint() throws Exception {
        ColorReading cRead = getReading();
        this.mRed = 255 / (double) cRead.red;
        this.mGreen = 255 / (double) cRead.green;
        this.mBlue = 255 / (double) cRead.blue;
        this.mClear = 765 / (double) cRead.clear;

    }

    /**
     * Sets the gain (hardware amplification) of readings.
     * @param gain amount of amplification
     * @throws IOException If there is a hardware I/O error
     */
    public void setGain(int gain) throws IOException {
        this.write8(TCS34725_CONTROL, (byte) gain);
    }

    /**
     * Standard read for I2C device (one byte)
     * @param reg location of register to read from
     * @return int value read from device
     * @throws IOException 
     */
    private int readU8(int reg) throws IOException {
        return this.device.read(COMMAND_BIT | reg);
    }

    /**
     * Standard read for two consecutive I2C device registers
     * @param reg location of first register in a consecutive pair to read from
     * @return value taken from both registers interpreted as one integer
     * @throws Exception 
     */
    private int readU16(int reg) throws Exception {
        int dataLow = this.readU8(COMMAND_BIT | reg);
        int dataHigh = this.readU8(COMMAND_BIT | reg + 1);
        return (dataHigh << 8) + dataLow;
    }

    /**
     * Writes one byte to an I2C device register
     * @param register Location of register
     * @param value Value to write
     * @throws IOException 
     */
    private void write8(int register, int value) throws IOException {
        this.device.write(COMMAND_BIT | register, (byte) (value & 0xff));
    }

    /**
     * Returns the max of three integers
     * @param a Integer to compare
     * @param b Integer to compare
     * @param c Integer to compare
     * @return value of largest compared integer
     */
    private int max(int a, int b, int c) {
        if (a >= b && a >= c) {
            return a;
        } else if (b >= a && b >= c) {
            return b;
        }

        return c;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private static void waitfor(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    /**
     * Simple subclass containing numerical color reading information
     */
    public class ColorReading {

        private int red, blue, green, clear;

        public ColorReading(int r, int b, int g, int c) {
            this.red = r;
            this.blue = b;
            this.green = g;
            this.clear = c;
        }

        public int getRed() {
            return this.red;
        }

        public int getBlue() {
            return this.blue;
        }

        public int getGreen() {
            return this.green;
        }

        public int getClear() {
            return this.clear;
        }

        @Override
        public String toString() {
            return "Red:" + Integer.toString(red)
                    + "\nBlue:" + Integer.toString(blue)
                    + "\nGreen:" + Integer.toString(green)
                    + "\nClear:" + Integer.toString(clear);
        }
    }
}
