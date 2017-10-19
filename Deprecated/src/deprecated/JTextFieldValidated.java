package deprecated;

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


import javax.swing.JTextField;

/**
 *
 * @author jason
 */
public class JTextFieldValidated extends JTextField{
    private boolean isInputValid = true;
    
    // I want to make this change permanent, but only for me
    
    public boolean isInputValid(){
        return this.isInputValid;
    }
}
