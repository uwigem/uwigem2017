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
package pumpDiagnostics;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import iGEM2017.SyringePump;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jason
 */
public class PumpDiagGui extends javax.swing.JFrame {

    private SyringePump pump1, pump2, pump3;

    // GPIO Controller for Raspberry Pi pins
    private final GpioController gpio;

    // GPIO expander chips
    private final MCP23017GpioProvider mcpProviderOne;
    private final MCP23017GpioProvider mcpProviderTwo;

    /**
     * Creates new form MainWindow
     */
    public PumpDiagGui() throws I2CFactory.UnsupportedBusNumberException, IOException, IOException {
        gpio = GpioFactory.getInstance(); // Singleton instance
        mcpProviderOne = new MCP23017GpioProvider(I2CBus.BUS_1, 0x27);
        mcpProviderTwo = new MCP23017GpioProvider(I2CBus.BUS_2, 0x26);

        // Provision direction control pins as outputs
        GpioPinDigitalOutput dir1 = gpio.provisionDigitalOutputPin(mcpProviderOne, MCP23017Pin.GPIO_A7, "Pump 1 Direction", PinState.LOW);
        GpioPinDigitalOutput dir2 = gpio.provisionDigitalOutputPin(mcpProviderOne, MCP23017Pin.GPIO_B0, "Pump 2 Direction", PinState.LOW);
        GpioPinDigitalOutput dir3 = gpio.provisionDigitalOutputPin(mcpProviderOne, MCP23017Pin.GPIO_A4, "Pump 3 Direction", PinState.LOW);

        // Provision step control pins as outputs
        GpioPinDigitalOutput step1 = gpio.provisionDigitalOutputPin(mcpProviderOne, MCP23017Pin.GPIO_A6, "Pump 1 Step", PinState.LOW);
        GpioPinDigitalOutput step2 = gpio.provisionDigitalOutputPin(mcpProviderOne, MCP23017Pin.GPIO_B1, "Pump 2 Step", PinState.LOW);
        GpioPinDigitalOutput step3 = gpio.provisionDigitalOutputPin(mcpProviderOne, MCP23017Pin.GPIO_A3, "Pump 3 Step", PinState.LOW);

        // Provision enable pins as outputs. 
        // Note that the pin state "high" means the pump is disabled;
        // the enable pump on the chip is inverted.
        GpioPinDigitalOutput enable1 = gpio.provisionDigitalOutputPin(mcpProviderOne, MCP23017Pin.GPIO_A5, "Pump 1 ~Enable", PinState.HIGH);
        GpioPinDigitalOutput enable2 = gpio.provisionDigitalOutputPin(mcpProviderOne, MCP23017Pin.GPIO_B7, "Pump 2 ~Enable", PinState.HIGH);
        GpioPinDigitalOutput enable3 = gpio.provisionDigitalOutputPin(mcpProviderOne, MCP23017Pin.GPIO_A2, "Pump 3 ~Enable", PinState.HIGH);

        // Provision end-stop pins as inputs. 
        // These pins tell the pump when the syringe
        // is in its MINIMUM possible position (empty).
        GpioPinDigitalInput min1 = gpio.provisionDigitalInputPin(this.mcpProviderTwo, MCP23017Pin.GPIO_A0, "Pump 1 Min", PinPullResistance.PULL_UP);
        GpioPinDigitalInput min2 = gpio.provisionDigitalInputPin(this.mcpProviderTwo, MCP23017Pin.GPIO_A0, "Pump 2 Min", PinPullResistance.PULL_UP);
        GpioPinDigitalInput min3 = gpio.provisionDigitalInputPin(this.mcpProviderTwo, MCP23017Pin.GPIO_A0, "Pump 3 Min", PinPullResistance.PULL_UP);

        // Provision end-stop pins as inputs.
        // These pins tell the pump when the syringe
        // is in its MAXIMUM possible position (full).
        GpioPinDigitalInput max1 = gpio.provisionDigitalInputPin(this.mcpProviderTwo, MCP23017Pin.GPIO_A0, "Pump 1 Min", PinPullResistance.PULL_UP);
        GpioPinDigitalInput max2 = gpio.provisionDigitalInputPin(this.mcpProviderTwo, MCP23017Pin.GPIO_A0, "Pump 2 Min", PinPullResistance.PULL_UP);
        GpioPinDigitalInput max3 = gpio.provisionDigitalInputPin(this.mcpProviderTwo, MCP23017Pin.GPIO_A0, "Pump 3 Min", PinPullResistance.PULL_UP);

        pump1 = new SyringePump(step1, dir1, enable1, min1, max1);
        pump2 = new SyringePump(step2, dir2, enable2, min2, max2);
        pump3 = new SyringePump(step3, dir3, enable3, min3, max3);

        pump1.setStepDelay(100);
        pump2.setStepDelay(100);
        pump3.setStepDelay(100);
        
        initComponents();

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel6 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButton5 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jButton7 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jButton8 = new javax.swing.JButton();
        labelDiagnosticMessage = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setText("Basic Pump Tests");

        jLabel3.setText("Click buttons to move pump slightly in the specified direction.");

        jButton2.setText("Dispense");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPressDispensePump1(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel2.setText("Pump 1");

        jButton1.setText("Fill");
        jButton1.setMaximumSize(new java.awt.Dimension(75, 23));
        jButton1.setMinimumSize(new java.awt.Dimension(75, 23));
        jButton1.setPreferredSize(new java.awt.Dimension(75, 23));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPressFillPump1(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        jButton5.setText("Dispense");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPressDispensePump2(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel5.setText("Pump 1");

        jButton6.setText("Fill");
        jButton6.setMaximumSize(new java.awt.Dimension(75, 23));
        jButton6.setMinimumSize(new java.awt.Dimension(75, 23));
        jButton6.setPreferredSize(new java.awt.Dimension(75, 23));
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPressFillPump2(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton5)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5))
                .addContainerGap())
        );

        jButton7.setText("Dispense");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPressDispensePump3(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel6.setText("Pump 1");

        jButton8.setText("Fill");
        jButton8.setMaximumSize(new java.awt.Dimension(75, 23));
        jButton8.setMinimumSize(new java.awt.Dimension(75, 23));
        jButton8.setPreferredSize(new java.awt.Dimension(75, 23));
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPressFillPump3(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton7)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton7))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        labelDiagnosticMessage.setText("Diagnostic information will appear here.");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelDiagnosticMessage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(labelDiagnosticMessage))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonPressFillPump1(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPressFillPump1
        if (pump1.canStep(SyringePump.Direction.FILL)) {

            try {
                pump1.takeSteps(25, SyringePump.Direction.FILL);
            } catch (InterruptedException ex) {
                Logger.getLogger(PumpDiagGui.class.getName()).log(Level.SEVERE, null, ex);
                labelDiagnosticMessage.setText("ERROR: Cannot fill pump due to a hardware error.");
            }
        } else {

            labelDiagnosticMessage.setText("Can't fill. The pump thinks it's at the maximum/full position already.");
        }
    }//GEN-LAST:event_buttonPressFillPump1

    private void buttonPressFillPump2(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPressFillPump2

        if (pump2.canStep(SyringePump.Direction.FILL)) {

            try {
                pump2.takeSteps(25, SyringePump.Direction.FILL);
            } catch (InterruptedException ex) {
                Logger.getLogger(PumpDiagGui.class.getName()).log(Level.SEVERE, null, ex);
                labelDiagnosticMessage.setText("ERROR: Cannot fill pump due to a hardware error.");
            }
        } else {

            labelDiagnosticMessage.setText("Can't fill. The pump thinks it's at the maximum/full position already.");
        }
    }//GEN-LAST:event_buttonPressFillPump2

    private void buttonPressFillPump3(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPressFillPump3

        if (pump3.canStep(SyringePump.Direction.FILL)) {

            try {
                pump3.takeSteps(25, SyringePump.Direction.FILL);
            } catch (InterruptedException ex) {
                Logger.getLogger(PumpDiagGui.class.getName()).log(Level.SEVERE, null, ex);
                labelDiagnosticMessage.setText("ERROR: Cannot fill pump due to a hardware error.");
            }
        } else {

            labelDiagnosticMessage.setText("Can't fill. The pump thinks it's at the maximum/full position already.");
        }
    }//GEN-LAST:event_buttonPressFillPump3

    private void buttonPressDispensePump1(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPressDispensePump1
        if (pump1.canStep(SyringePump.Direction.DISPENSE)) {

            try {
                labelDiagnosticMessage.setText("Trying to dispense a few steps steps on pump 1");
                pump1.takeSteps(25, SyringePump.Direction.DISPENSE);

            } catch (InterruptedException ex) {
                Logger.getLogger(PumpDiagGui.class.getName()).log(Level.SEVERE, null, ex);
                labelDiagnosticMessage.setText("ERROR: Cannot dispense from pump due to hardware error.");
            }
        } else {
            labelDiagnosticMessage.setText("Can't dispense. The pump thinks it's at the minimum position already.");
        }
    }//GEN-LAST:event_buttonPressDispensePump1

    private void buttonPressDispensePump2(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPressDispensePump2
        if (pump2.canStep(SyringePump.Direction.DISPENSE)) {

            try {
                labelDiagnosticMessage.setText("Trying to dispense a few steps steps on pump 1");
                pump2.takeSteps(25, SyringePump.Direction.DISPENSE);

            } catch (InterruptedException ex) {
                Logger.getLogger(PumpDiagGui.class.getName()).log(Level.SEVERE, null, ex);
                labelDiagnosticMessage.setText("ERROR: Cannot dispense from pump due to hardware error.");
            }
        } else {
            labelDiagnosticMessage.setText("Can't dispense. The pump thinks it's at the minimum position already.");
        }
    }//GEN-LAST:event_buttonPressDispensePump2

    private void buttonPressDispensePump3(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPressDispensePump3
        if (pump3.canStep(SyringePump.Direction.DISPENSE)) {

            try {
                labelDiagnosticMessage.setText("Trying to dispense a few steps steps on pump 3");
                pump3.takeSteps(25, SyringePump.Direction.DISPENSE);

            } catch (InterruptedException ex) {
                Logger.getLogger(PumpDiagGui.class.getName()).log(Level.SEVERE, null, ex);
                labelDiagnosticMessage.setText("ERROR: Cannot dispense from pump due to hardware error.");
            }
        } else {
            labelDiagnosticMessage.setText("Can't dispense. The pump thinks it's at the minimum position already.");
        }
    }//GEN-LAST:event_buttonPressDispensePump3

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PumpDiagGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PumpDiagGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PumpDiagGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PumpDiagGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new PumpDiagGui().setVisible(true);
                } catch (I2CFactory.UnsupportedBusNumberException ex) {
                    Logger.getLogger(PumpDiagGui.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(PumpDiagGui.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JLabel labelDiagnosticMessage;
    // End of variables declaration//GEN-END:variables
}
