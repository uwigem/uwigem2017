package deprecated;



/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Java Examples
 * FILENAME      :  I2CExample.java
 *
 * This file is part of the Pi4J project. More information about
 * this project can be found here:  http://www.pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2017 Pi4J
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.IOException;
import java.util.Arrays;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.pi4j.platform.PlatformAlreadyAssignedException;
import com.pi4j.util.Console;

/**
 * This example code demonstrates how to perform simple I2C
 * communication on the Raspberry Pi.  For this example we will
 * connect to a 'TSL2561' LUX sensor.
 *
 * Data Sheet:
 * https://www.adafruit.com/datasheets/TSL256x.pdf
 *
 * You should get something similar printed in the console
 * when executing this program:
 *
 * > <--Pi4J--> I2C Example ... started.
 * > ... reading ID register from TSL2561
 * > TSL2561 ID = 0x50 (should be 0x50)
 * > ... powering up TSL2561
 * > ... reading DATA registers from TSL2561
 * > TSL2561 DATA 0 = 0x1e
 * > TSL2561 DATA 1 = 0x04
 * > ... powering down TSL2561
 * > Exiting bananapi.I2CExample
 *
 *
 * @author Robert Savage
 */
public class I2CExample {
public static final byte TCS34725_ADDRESS       =  (byte)(0x29);
public static final byte TCS34725_COMMAND_BIT   =  (byte)(0x80);

public static final byte TCS34725_ENABLE        =  (byte)(0x00);
public static final byte TCS34725_ENABLE_AIEN   =  (byte)(0x10);    /* RGBC Interrupt Enable */
public static final byte TCS34725_ENABLE_WEN    =  (byte)(0x08);    /* Wait enable - Writing 1 activates the wait timer */
public static final byte TCS34725_ENABLE_AEN    =  (byte)(0x02);    /* RGBC Enable - Writing 1 actives the ADC, 0 disables it */
public static final byte TCS34725_ENABLE_PON    =  (byte)(0x01);    /* Power on - Writing 1 activates the internal oscillator, 0 disables it */
public static final byte TCS34725_ATIME         =  (byte)(0x01);    /* Integration time */
public static final byte TCS34725_WTIME         =  (byte)(0x03);    /* Wait time (if TCS34725_ENABLE_WEN is asserted) */
public static final byte TCS34725_WTIME_2_4MS   =  (byte)(0xFF);    /* WLONG0 = 2.4ms   WLONG1 = 0.029s */
public static final byte TCS34725_WTIME_204MS   =  (byte)(0xAB);    /* WLONG0 = 204ms   WLONG1 = 2.45s  */
public static final byte TCS34725_WTIME_614MS   =  (byte)(0x00);    /* WLONG0 = 614ms   WLONG1 = 7.4s   */
public static final byte TCS34725_AILTL         =  (byte)(0x04);    /* Clear channel lower interrupt threshold */
public static final byte TCS34725_AILTH         =  (byte)(0x05);
public static final byte TCS34725_AIHTL         =  (byte)(0x06);    /* Clear channel upper interrupt threshold */
public static final byte TCS34725_AIHTH         =  (byte)(0x07);
public static final byte TCS34725_PERS          =  (byte)(0x0C);    /* Persistence register - basic SW filtering mechanism for interrupts */
public static final byte TCS34725_CONFIG        =  (byte)(0x0D);
public static final byte TCS34725_CONFIG_WLONG  =  (byte)(0x02);    /* Choose between short and long (12x) wait times via TCS34725_WTIME */
public static final byte TCS34725_CONTROL       =  (byte)(0x0F);    /* Set the gain level for the sensor */
public static final byte TCS34725_ID            =  (byte)(0x12);    /* 0x44 = TCS34721/TCS34725, 0x4D = TCS34723/TCS34727 */
public static final byte TCS34725_STATUS        =  (byte)(0x13);
public static final byte TCS34725_STATUS_AINT   =  (byte)(0x10);    /* RGBC Clean channel interrupt */
public static final byte TCS34725_STATUS_AVALID =  (byte)(0x01);    /* Indicates that the RGBC channels have completed an integration cycle */
public static final byte TCS34725_CDATAL        =  (byte)(0x14);    /* Clear channel data */
public static final byte TCS34725_CDATAH        =  (byte)(0x15);
public static final byte TCS34725_RDATAL        =  (byte)(0x16);    /* Red channel data */
public static final byte TCS34725_RDATAH        =  (byte)(0x17);
public static final byte TCS34725_GDATAL        =  (byte)(0x18);    /* Green channel data */
public static final byte TCS34725_GDATAH        =  (byte)(0x19);
public static final byte TCS34725_BDATAL        =  (byte)(0x1A);    /* Blue channel data */
public static final byte TCS34725_BDATAH        =  (byte)(0x1B);
    
    /**
     * Program Main Entry Point
     *
     * @param args
     * @throws InterruptedException
     * @throws PlatformAlreadyAssignedException
     * @throws IOException
     * @throws UnsupportedBusNumberException
     */
    public static void main(String[] args) throws InterruptedException, PlatformAlreadyAssignedException, IOException, UnsupportedBusNumberException {

        System.out.println("This is the one");
        
        // create Pi4J console wrapper/helper
        // (This is a utility class to abstract some of the boilerplate code)
        final Console console = new Console();

        // print program title/header
        console.title("<-- The Pi4J Project -->", "I2C Example");

        // allow for user to exit program using CTRL-C
        console.promptForExit();

        // fetch all available busses
        /*try {
            int[] ids = I2CFactory.getBusIds();
            console.println("Found follow I2C busses: " + Arrays.toString(ids));
        } catch (IOException exception) {
            console.println("I/O error during fetch of I2C busses occurred");
        }*/
        
        // find available busses
        for (int number = I2CBus.BUS_0; number <= I2CBus.BUS_17; ++number) {
            try {
                I2CBus bus = I2CFactory.getInstance(number);
                console.println("Supported I2C bus " + number + " found");
            } catch (IOException exception) {
                console.println("I/O error on I2C bus " + number + " occurred");
            } catch (UnsupportedBusNumberException exception) {
                console.println("Unsupported I2C bus " + number + " required");
            }
        }
        
        // get the I2C bus to communicate on
        I2CBus i2c = I2CFactory.getInstance(I2CBus.BUS_1);
        

        // create an I2C device for an individual device on the bus that you want to communicate with
        // in this example we will use the default address for the TSL2561 chip which is 0x39.
        I2CDevice device = i2c.getDevice(TCS34725_ADDRESS);
        
        // Activate the device
        device.write(TCS34725_ADDRESS, TCS34725_ENABLE_PON);

        // next, lets perform am I2C READ operation to the TSL2561 chip
        // we will read the 'ID' register from the chip to get its part number and silicon revision number
        console.println("... reading ID register");
        int response = device.read(18);
        
        
        
        console.println("TCS34725 ID = " + String.format("0x%02x", response));

        // next we want to start taking light measurements, so we need to power up the sensor
        console.println("... powering up TSL2561");

        // wait while the chip collects data
        Thread.sleep(500);

        // now we will perform our first I2C READ operation to retrieve raw integration
        // results from DATA_0 and DATA_1 registers
        /*console.println("... reading DATA registers from TSL2561");
        int data0 = device.read(TSL2561_REG_DATA_0);
        int data1 = device.read(TSL2561_REG_DATA_1);

        // print raw integration results from DATA_0 and DATA_1 registers
        console.println("TSL2561 DATA 0 = " + String.format("0x%02x", data0));
        console.println("TSL2561 DATA 1 = " + String.format("0x%02x", data1));

        // before we exit, lets not forget to power down light sensor
        console.println("... powering down TSL2561");
        device.write(TSL2561_REG_CONTROL, TSL2561_POWER_DOWN);*/
        
    }
}
