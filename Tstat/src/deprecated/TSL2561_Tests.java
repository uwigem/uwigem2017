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
package deprecated;

import iGEM2017.LuxSensor;
import com.pi4j.io.i2c.I2CFactory;
import java.io.IOException;

/**
 *
 * @author Washington iGEM Team 2017
 */
public class TSL2561_Tests {

    public static void main(String[] args)
            throws InterruptedException, IOException, I2CFactory.UnsupportedBusNumberException {

        System.out.println("<-----  STARTING TSL2651 UNIT TESTS  ----->");

        LuxSensor sensor1 = new LuxSensor((byte) 0x39);
        LuxSensor sensor2 = new LuxSensor((byte) 0x49);


        while (System.in.available() == 0) {
            Thread.sleep(400);
            double r1 = sensor1.read();
            double r2 = sensor2.read();

            
            
            System.out.println("Sensor 1: " + r1 + " lux");
            System.out.println("Sensor 2: " + r2 + " lux");
        }
    }

}
