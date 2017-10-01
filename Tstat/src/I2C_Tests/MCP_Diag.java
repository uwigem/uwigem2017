/*
 * Copyright (C) 2017 jason
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

import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.i2c.I2CFactory;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author jason
 */
public class MCP_Diag {
    public static void main(String[] args) throws I2CFactory.UnsupportedBusNumberException, IOException {
        MCP23017_Control controller = new MCP23017_Control(0x21);
        
        Scanner scan = new Scanner(System.in);
        int pinInt;
        int onOff;
        int inOut;
        
        
        while(true){
            
            System.out.println("Select a pin");
            pinInt = scan.nextInt();
            System.out.println("Set input(0) or output(1)");
            
            inOut = scan.nextInt();
            
            if(inOut == 0 && pinInt >= 0 && pinInt <=7){
                controller.provisionInput(intToPin(pinInt));                
            } else {
                System.out.println("Set pin output state to 0 or 1?");
                onOff = scan.nextInt();
                if(onOff == 0) {
                controller.provisionOutput(intToPin(pinInt), PinState.HIGH); }
                else if(onOff == 1){
                    controller.provisionOutput(intToPin(pinInt), PinState.LOW);
                }
            }
        }
    }
    
    public static Pin intToPin(int i){
        return MCP23017Pin.ALL[i];
    }
}
