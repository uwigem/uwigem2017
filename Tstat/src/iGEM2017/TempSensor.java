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

import com.pi4j.io.i2c.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * 
 *
 * @author Washington iGEM Team 2017
 */
public class TempSensor {
    
    public enum MEASURE{CELSIUS,FAHRENHEIT}
    
    private I2CBus bus;
    private I2CDevice device;
    
    public TempSensor(){
        try {
            this.bus = I2CFactory.getInstance(I2CBus.BUS_1);
            this.device = bus.getDevice(0x40);
        } catch (I2CFactory.UnsupportedBusNumberException ex) {
            System.out.println("Temp/Humidity sensor error: Couldn't access the I2C bus.");
            Logger.getLogger(TempSensor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TempSensor.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Temp/Humidity sensor error: General I/O error");
        }
		
    }
    
    /**
     * Description of method in plain language
     * @param measure Meaning of parameter
     * @return What it returns
     */
   public double getReading(TempSensor.MEASURE measure){
       byte[] data = new byte[2];
       
        try {
            // Send command to get temp data
            device.write((byte)0xF3);
        } catch (IOException ex) {
            System.out.println("Error getting temperature data: couldn't issue command");
            Logger.getLogger(TempSensor.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            // Wait for integration
            Thread.sleep(250);
        } catch (InterruptedException ex) {
            System.out.println("Error while sleeping thread. Can't get temp data.");
            Logger.getLogger(TempSensor.class.getName()).log(Level.SEVERE, null, ex);            
        }

        
        // Read 2 bytes of temperature data, msb first
        
        try {
            device.read(data, 0, 2);
        } catch (IOException ex) {
            Logger.getLogger(TempSensor.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error getting temp data: general I/O problem.");
        }

        // Convert temperature data depending on requested measure
        
        double cTemp = (((((data[0] & 0xFF) * 256) + (data[1] & 0xFF)) * 175.72) / 65536.0) - 46.85;
        
        if(measure == MEASURE.CELSIUS){
            return (cTemp);
        }
        
        // Then it's MEASURE.FAHRENHEIT
        return (cTemp * 1.8 ) + 32;
   }
   
   public double getHumidity() {
       double result = 0;
       
        try {
            // Send humidity measurement command
            device.write((byte)0xF5);
            Thread.sleep(300);
            
            // Read 2 bytes of humidity data, msb first
            byte[] data = new byte[2];
            device.read(data, 0, 2);
            
            // Convert humidity data
            result = (((((data[0] & 0xFF) * 256) + (data[1] & 0xFF)) * 125.0) / 65536.0) - 6;
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(TempSensor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;        
   }
}
