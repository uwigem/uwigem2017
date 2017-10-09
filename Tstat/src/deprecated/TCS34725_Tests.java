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

import iGEM2017.RgbSensor;
import com.pi4j.io.i2c.I2CFactory;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Washington iGEM Team 2017
 */
public class TCS34725_Tests {

    public static void main(String[] args)
            throws IOException, I2CFactory.UnsupportedBusNumberException, Exception {

        RgbSensor sensor;
        sensor = new iGEM2017.RgbSensor();

        while (System.in.available() == 0) {
            RgbSensor.ColorReading color = sensor.getReading();

            System.out.println(color.toString());
            Thread.sleep(500);
        }
    }

}
