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

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.UIManager;

/**
 * Constitutes the main graphical interface for the chromastat. The UI is build
 * on Java Swing for use on an ARM32 device (the Raspberry Pi).
 *
 * @author Washington iGEM Team 2017
 */
public class MainWindow extends javax.swing.JFrame {

    /**
     * Creates new form MainWindow
     */
    private boolean isRecording = false;
    private SyringePump pump1, pump2, pump3;

    // GPIO Controller for Raspberry Pi pins
    private final GpioController gpio;

    // GPIO expander chips
    private final MCP23017GpioProvider mcpProviderOne;
    private final MCP23017GpioProvider mcpProviderTwo;

    // Sensor controllers
    private final RgbSensor colorRead;
    private final TempSensor tempRead;
    private final LuxSensor lightRead;

    public MainWindow() throws IOException, I2CFactory.UnsupportedBusNumberException, InterruptedException {
        initComponents();
        gpio = GpioFactory.getInstance(); // Singleton instance
        mcpProviderOne = new MCP23017GpioProvider(I2CBus.BUS_1, 0x27);
        mcpProviderTwo = new MCP23017GpioProvider(I2CBus.BUS_2, 0x26);
        // Initialize syringe pumps
        initPumps();

        colorRead = new RgbSensor();
        tempRead = new TempSensor();
        lightRead = new LuxSensor();
        DecimalFormat d = new DecimalFormat("#.#######");
        BufferedWriter csvFile = new BufferedWriter(new FileWriter("file.csv"));
        csvFile.write("Color, Lux, Temp, Humidity");
        csvFile.newLine();

        Timer actionTime = new Timer(300, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                double tempNum;
                double humidityNum;
                double lightNum;
                try {
                    RgbSensor.ColorReading color;
                    color = colorRead.getNormalizedReading();
                    Color colorPanelBackground = new Color(color.getRed(), color.getGreen(), color.getBlue());
                    colorPanel.setBackground(colorPanelBackground);
                    colorStringLabel.setText(color.getRed() + "," + color.getGreen() + "," + color.getBlue());
                    huePanel.setBackground(colorRead.readingToHue(color));
                    Color brightnessPanelColor = new Color(color.getClear(), color.getClear(), color.getClear());
                    brightnessPanel.setBackground(brightnessPanelColor);

                    tempNum = tempRead.getReading(TempSensor.MEASURE.CELSIUS);
                    tempNumLabel.setText(d.format(tempNum));

                    humidityNum = tempRead.getHumidity();
                    humidityNumLabel.setText(d.format(humidityNum));

                    if (lightRead.getReading() < .00035) {
                        lightNum = lightRead.getReading();
                        lightNumLabel.setText("0");
                    } else {
                        lightNum = lightRead.getReading();
                        lightNumLabel.setText(d.format(lightNum));
                    }
                    if (isRecording == true) {
                        csvFile.write(color.getRed() + "." + color.getGreen() + "." + color.getBlue() + "," + lightNum + "," + tempNum + "," + humidityNum);
                        csvFile.newLine();
                    }
                } catch (Exception ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        actionTime.start();

    }

    private void initPumps() {

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
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        buttonGroup6 = new javax.swing.ButtonGroup();
        tabPane = new javax.swing.JTabbedPane();
        colorLabel = new javax.swing.JPanel();
        buttonExit = new javax.swing.JButton();
        lightSensorTitleLabel = new javax.swing.JLabel();
        colorSensorTitleLabel = new javax.swing.JLabel();
        tempSensorTitleLabel = new javax.swing.JLabel();
        turbiditySensorTitleLabel = new javax.swing.JLabel();
        lightNumLabel = new javax.swing.JLabel();
        colorStringLabel = new javax.swing.JLabel();
        turbidityNumLabel = new javax.swing.JLabel();
        tempNumLabel = new javax.swing.JLabel();
        brightnessPanel = new javax.swing.JPanel();
        sensorTitle = new javax.swing.JLabel();
        pump1TitleLabel = new javax.swing.JLabel();
        pump2TitleLabel = new javax.swing.JLabel();
        pump3TitleLabel = new javax.swing.JLabel();
        fluid1Label = new javax.swing.JLabel();
        fluid2Label = new javax.swing.JLabel();
        fluid3Label = new javax.swing.JLabel();
        volume1Label = new javax.swing.JLabel();
        volume2Label = new javax.swing.JLabel();
        volume3Label = new javax.swing.JLabel();
        pumpBar1 = new javax.swing.JProgressBar();
        pumpBar2 = new javax.swing.JProgressBar();
        pumpBar3 = new javax.swing.JProgressBar();
        humidityTilteLabel = new javax.swing.JLabel();
        humidityNumLabel = new javax.swing.JLabel();
        huePanel = new javax.swing.JPanel();
        colorPanel = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        hueLabel = new javax.swing.JLabel();
        brightnessLabel = new javax.swing.JLabel();
        recordingButton = new javax.swing.JButton();
        pumpsAllTitleLabel1 = new javax.swing.JLabel();
        callibratePanel = new javax.swing.JPanel();
        motor1DispenseButton = new javax.swing.JButton();
        motor1EmptyVolField = new javax.swing.JTextField();
        motor1EmptyVolLabel = new javax.swing.JLabel();
        motor1FillButton = new javax.swing.JButton();
        motor1FullVolField = new javax.swing.JTextField();
        motor1FullVolLabel = new javax.swing.JLabel();
        motor1CallibrateButton = new javax.swing.JButton();
        motor1WorkingDoneLabel = new javax.swing.JLabel();
        motor2EmptyVolField = new javax.swing.JTextField();
        motor2EmptyVolLabel = new javax.swing.JLabel();
        motor2CallibrateButton = new javax.swing.JButton();
        motor2WorkingDoneLabel = new javax.swing.JLabel();
        motor2FillButton = new javax.swing.JButton();
        motor2FullVolField = new javax.swing.JTextField();
        motor2FullVolLabel = new javax.swing.JLabel();
        motor2DispenseButton = new javax.swing.JButton();
        motor3EmptyVolField = new javax.swing.JTextField();
        motor3EmptyVolLabel = new javax.swing.JLabel();
        motor3CallibrateButton = new javax.swing.JButton();
        motor3WorkingDoneLabel = new javax.swing.JLabel();
        motor3FillButton = new javax.swing.JButton();
        motor3FullVolField = new javax.swing.JTextField();
        motor3FullVolLabel = new javax.swing.JLabel();
        motor3DispenseButton = new javax.swing.JButton();
        editValuesPanel = new javax.swing.JPanel();
        toggleTitle = new javax.swing.JLabel();
        stirrerLabel = new javax.swing.JLabel();
        interiorLightingLabel = new javax.swing.JLabel();
        bubblerLabel = new javax.swing.JLabel();
        laserLabel = new javax.swing.JLabel();
        targetValuesTitle = new javax.swing.JLabel();
        targetTempLabel = new javax.swing.JLabel();
        targetTempField = new javax.swing.JTextField();
        celciusRB = new javax.swing.JRadioButton();
        fahrenheitRB = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        pumpsLabel = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        dispenseLabel1 = new javax.swing.JLabel();
        dispenseLabel2 = new javax.swing.JLabel();
        dispenseLabel3 = new javax.swing.JLabel();
        dispenseField1 = new javax.swing.JTextField();
        dispenseField2 = new javax.swing.JTextField();
        dispenseField3 = new javax.swing.JTextField();
        mlLabel1 = new javax.swing.JLabel();
        mlLabel2 = new javax.swing.JLabel();
        mlLabel3 = new javax.swing.JLabel();
        goButton1 = new javax.swing.JButton();
        abortButton2 = new javax.swing.JButton();
        abortButton3 = new javax.swing.JButton();
        refillButton2 = new javax.swing.JButton();
        refillButton1 = new javax.swing.JButton();
        refillButton3 = new javax.swing.JButton();
        submitButton = new javax.swing.JButton();
        stirrerToggle = new javax.swing.JToggleButton();
        interiorLightingToggle = new javax.swing.JToggleButton();
        bubblerToggle = new javax.swing.JToggleButton();
        laserToggle = new javax.swing.JToggleButton();
        jLabel10 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        abortButton1 = new javax.swing.JButton();
        goButton2 = new javax.swing.JButton();
        goButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("\"Exit?\"");
        setBounds(new java.awt.Rectangle(0, 0, 800, 480));
        setMinimumSize(new java.awt.Dimension(800, 480));
        setName("mainWindowFrame"); // NOI18N
        setUndecorated(true);
        setResizable(false);

        tabPane.setPreferredSize(new java.awt.Dimension(799, 488));

        buttonExit.setText("Exit");
        buttonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonExitActionPerformed(evt);
            }
        });

        lightSensorTitleLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lightSensorTitleLabel.setText("Light:");
        lightSensorTitleLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lightSensorTitleLabel.setMaximumSize(new java.awt.Dimension(50, 20));
        lightSensorTitleLabel.setMinimumSize(new java.awt.Dimension(50, 20));

        colorSensorTitleLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        colorSensorTitleLabel.setText("Color:");
        colorSensorTitleLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        colorSensorTitleLabel.setMaximumSize(new java.awt.Dimension(50, 20));
        colorSensorTitleLabel.setMinimumSize(new java.awt.Dimension(50, 20));

        tempSensorTitleLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        tempSensorTitleLabel.setText("Temperature:");
        tempSensorTitleLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        tempSensorTitleLabel.setMaximumSize(new java.awt.Dimension(50, 20));
        tempSensorTitleLabel.setMinimumSize(new java.awt.Dimension(50, 20));

        turbiditySensorTitleLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        turbiditySensorTitleLabel.setText("Turbidity");
        turbiditySensorTitleLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        turbiditySensorTitleLabel.setMaximumSize(new java.awt.Dimension(50, 20));
        turbiditySensorTitleLabel.setMinimumSize(new java.awt.Dimension(50, 20));

        lightNumLabel.setText("lightNum");
        lightNumLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lightNumLabel.setMaximumSize(new java.awt.Dimension(50, 20));
        lightNumLabel.setMinimumSize(new java.awt.Dimension(50, 20));

        colorStringLabel.setText("colorString");
        colorStringLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        colorStringLabel.setMaximumSize(new java.awt.Dimension(50, 20));
        colorStringLabel.setMinimumSize(new java.awt.Dimension(50, 20));

        turbidityNumLabel.setText("turbidityNum");
        turbidityNumLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        turbidityNumLabel.setMaximumSize(new java.awt.Dimension(50, 20));
        turbidityNumLabel.setMinimumSize(new java.awt.Dimension(50, 20));

        tempNumLabel.setText("tempNum");
        tempNumLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        tempNumLabel.setMaximumSize(new java.awt.Dimension(50, 20));
        tempNumLabel.setMinimumSize(new java.awt.Dimension(50, 20));

        brightnessPanel.setBackground(new java.awt.Color(255, 204, 255));
        brightnessPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        brightnessPanel.setPreferredSize(new java.awt.Dimension(50, 50));

        javax.swing.GroupLayout brightnessPanelLayout = new javax.swing.GroupLayout(brightnessPanel);
        brightnessPanel.setLayout(brightnessPanelLayout);
        brightnessPanelLayout.setHorizontalGroup(
            brightnessPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 48, Short.MAX_VALUE)
        );
        brightnessPanelLayout.setVerticalGroup(
            brightnessPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 48, Short.MAX_VALUE)
        );

        sensorTitle.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        sensorTitle.setText("Sensors:");

        pump1TitleLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        pump1TitleLabel.setText("Pump 1:");
        pump1TitleLabel.setToolTipText("");

        pump2TitleLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        pump2TitleLabel.setText("Pump 2:");

        pump3TitleLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        pump3TitleLabel.setText("Pump 3:");

        fluid1Label.setText("fluid1");

        fluid2Label.setText("fluid2");

        fluid3Label.setText("fluid3");

        volume1Label.setText("volume 1");

        volume2Label.setText("volume 2");

        volume3Label.setText("volume 3");

        humidityTilteLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        humidityTilteLabel.setText("Humidity:");
        humidityTilteLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        humidityTilteLabel.setMaximumSize(new java.awt.Dimension(50, 20));
        humidityTilteLabel.setMinimumSize(new java.awt.Dimension(50, 20));

        humidityNumLabel.setText("humidityNum");
        humidityNumLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        humidityNumLabel.setMaximumSize(new java.awt.Dimension(50, 20));
        humidityNumLabel.setMinimumSize(new java.awt.Dimension(50, 20));

        huePanel.setBackground(new java.awt.Color(255, 204, 255));
        huePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        huePanel.setPreferredSize(new java.awt.Dimension(50, 50));

        javax.swing.GroupLayout huePanelLayout = new javax.swing.GroupLayout(huePanel);
        huePanel.setLayout(huePanelLayout);
        huePanelLayout.setHorizontalGroup(
            huePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 48, Short.MAX_VALUE)
        );
        huePanelLayout.setVerticalGroup(
            huePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 48, Short.MAX_VALUE)
        );

        colorPanel.setBackground(new java.awt.Color(255, 204, 255));
        colorPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        colorPanel.setPreferredSize(new java.awt.Dimension(50, 50));

        javax.swing.GroupLayout colorPanelLayout = new javax.swing.GroupLayout(colorPanel);
        colorPanel.setLayout(colorPanelLayout);
        colorPanelLayout.setHorizontalGroup(
            colorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 48, Short.MAX_VALUE)
        );
        colorPanelLayout.setVerticalGroup(
            colorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 48, Short.MAX_VALUE)
        );

        jLabel6.setText("Color:");

        hueLabel.setText("Hue:");

        brightnessLabel.setText("Brightness:");

        recordingButton.setText("Start Recording");
        recordingButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                recordingButtonMouseClicked(evt);
            }
        });

        pumpsAllTitleLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        pumpsAllTitleLabel1.setText("Pumps:");

        javax.swing.GroupLayout colorLabelLayout = new javax.swing.GroupLayout(colorLabel);
        colorLabel.setLayout(colorLabelLayout);
        colorLabelLayout.setHorizontalGroup(
            colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(colorLabelLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(colorLabelLayout.createSequentialGroup()
                        .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pump1TitleLabel)
                            .addComponent(pump2TitleLabel)
                            .addComponent(pump3TitleLabel))
                        .addGap(27, 27, 27)
                        .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fluid1Label)
                            .addComponent(fluid2Label)
                            .addComponent(fluid3Label))
                        .addGap(32, 32, 32)
                        .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(volume1Label)
                            .addComponent(volume2Label)
                            .addComponent(volume3Label))
                        .addGap(33, 33, 33)
                        .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pumpBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pumpBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pumpBar3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(recordingButton, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(buttonExit, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(63, 63, 63))
                    .addGroup(colorLabelLayout.createSequentialGroup()
                        .addComponent(sensorTitle)
                        .addContainerGap())))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, colorLabelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(colorLabelLayout.createSequentialGroup()
                        .addComponent(humidityTilteLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                        .addGap(24, 24, 24))
                    .addGroup(colorLabelLayout.createSequentialGroup()
                        .addComponent(turbiditySensorTitleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(27, 27, 27))
                    .addComponent(tempSensorTitleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(colorLabelLayout.createSequentialGroup()
                        .addComponent(colorSensorTitleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(46, 46, 46))
                    .addGroup(colorLabelLayout.createSequentialGroup()
                        .addComponent(lightSensorTitleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(47, 47, 47)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(colorStringLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lightNumLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(humidityNumLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(tempNumLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(turbidityNumLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(47, 47, 47)
                .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(colorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(84, 84, 84)
                .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(huePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hueLabel))
                .addGap(84, 84, 84)
                .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(brightnessLabel)
                    .addComponent(brightnessPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 250, Short.MAX_VALUE))
            .addGroup(colorLabelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pumpsAllTitleLabel1)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        colorLabelLayout.setVerticalGroup(
            colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(colorLabelLayout.createSequentialGroup()
                .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(colorLabelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(sensorTitle)
                        .addGap(8, 8, 8)
                        .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(colorLabelLayout.createSequentialGroup()
                                .addComponent(lightSensorTitleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(colorSensorTitleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
                                    .addComponent(colorStringLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(colorLabelLayout.createSequentialGroup()
                                .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(lightNumLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel6)
                                    .addComponent(hueLabel)
                                    .addComponent(brightnessLabel))
                                .addGap(18, 18, 18)
                                .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(huePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(colorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(brightnessPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 11, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tempSensorTitleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)
                            .addComponent(tempNumLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(turbiditySensorTitleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                            .addComponent(turbidityNumLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(humidityNumLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(humidityTilteLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pumpsAllTitleLabel1)
                        .addGap(14, 14, 14)
                        .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(pump1TitleLabel)
                                .addComponent(fluid1Label)
                                .addComponent(volume1Label))
                            .addComponent(pumpBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(28, 28, 28)
                        .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(pump2TitleLabel)
                            .addComponent(fluid2Label)
                            .addComponent(volume2Label)
                            .addComponent(pumpBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(36, 36, 36)
                        .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pump3TitleLabel)
                            .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(pumpBar3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(colorLabelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(fluid3Label)
                                    .addComponent(volume3Label))))
                        .addGap(8, 8, 8))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, colorLabelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(recordingButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(buttonExit, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(105, 105, 105))
        );

        tabPane.addTab("Sensors", colorLabel);

        motor1DispenseButton.setText("Dispense Completely");
        motor1DispenseButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                motor1DispenseButtonMousePressed(evt);
            }
        });

        motor1EmptyVolLabel.setText("Volume at completion (ml)");

        motor1FillButton.setText("Fill Completely");
        motor1FillButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                motor1FillButtonMousePressed(evt);
            }
        });

        motor1FullVolLabel.setText("Volume at full (ml)");

        motor1CallibrateButton.setText("Calibrate Motor 1");

        motor1WorkingDoneLabel.setText("Working...");

        motor2EmptyVolLabel.setText("Volume at completion (ml)");

        motor2CallibrateButton.setText("Calibrate Motor 2");

        motor2WorkingDoneLabel.setText("Working...");

        motor2FillButton.setText("Fill Completely");
        motor2FillButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                motor2FillButtonMousePressed(evt);
            }
        });

        motor2FullVolLabel.setText("Volume at full (ml)");

        motor2DispenseButton.setText("Dispense Completely");
        motor2DispenseButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                motor2DispenseButtonMousePressed(evt);
            }
        });

        motor3EmptyVolLabel.setText("Volume at completion (ml)");

        motor3CallibrateButton.setText("Calibrate Motor 3");

        motor3WorkingDoneLabel.setText("Working...");

        motor3FillButton.setText("Fill Completely");
        motor3FillButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                motor3FillButtonMousePressed(evt);
            }
        });

        motor3FullVolLabel.setText("Volume at full (ml)");

        motor3DispenseButton.setText("Dispense Completely");
        motor3DispenseButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                motor3DispenseButtonMousePressed(evt);
            }
        });

        javax.swing.GroupLayout callibratePanelLayout = new javax.swing.GroupLayout(callibratePanel);
        callibratePanel.setLayout(callibratePanelLayout);
        callibratePanelLayout.setHorizontalGroup(
            callibratePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(callibratePanelLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(callibratePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(callibratePanelLayout.createSequentialGroup()
                        .addComponent(motor1CallibrateButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(motor1WorkingDoneLabel))
                    .addComponent(motor1FillButton)
                    .addComponent(motor1DispenseButton)
                    .addGroup(callibratePanelLayout.createSequentialGroup()
                        .addComponent(motor1EmptyVolField, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(motor1EmptyVolLabel))
                    .addGroup(callibratePanelLayout.createSequentialGroup()
                        .addComponent(motor1FullVolField, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(motor1FullVolLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(callibratePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(callibratePanelLayout.createSequentialGroup()
                        .addComponent(motor2CallibrateButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(motor2WorkingDoneLabel))
                    .addComponent(motor2FillButton)
                    .addComponent(motor2DispenseButton)
                    .addGroup(callibratePanelLayout.createSequentialGroup()
                        .addComponent(motor2EmptyVolField, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(motor2EmptyVolLabel))
                    .addGroup(callibratePanelLayout.createSequentialGroup()
                        .addComponent(motor2FullVolField, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(motor2FullVolLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(callibratePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(callibratePanelLayout.createSequentialGroup()
                        .addComponent(motor3CallibrateButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(motor3WorkingDoneLabel))
                    .addComponent(motor3FillButton)
                    .addComponent(motor3DispenseButton)
                    .addGroup(callibratePanelLayout.createSequentialGroup()
                        .addComponent(motor3EmptyVolField, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(motor3EmptyVolLabel))
                    .addGroup(callibratePanelLayout.createSequentialGroup()
                        .addComponent(motor3FullVolField, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(motor3FullVolLabel)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        callibratePanelLayout.setVerticalGroup(
            callibratePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(callibratePanelLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(callibratePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(callibratePanelLayout.createSequentialGroup()
                        .addComponent(motor3DispenseButton)
                        .addGap(18, 18, 18)
                        .addGroup(callibratePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(motor3EmptyVolField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(motor3EmptyVolLabel))
                        .addGap(18, 18, 18)
                        .addComponent(motor3FillButton)
                        .addGap(18, 18, 18)
                        .addGroup(callibratePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(motor3FullVolField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(motor3FullVolLabel))
                        .addGap(18, 18, 18)
                        .addGroup(callibratePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(motor3CallibrateButton)
                            .addComponent(motor3WorkingDoneLabel)))
                    .addGroup(callibratePanelLayout.createSequentialGroup()
                        .addComponent(motor2DispenseButton)
                        .addGap(18, 18, 18)
                        .addGroup(callibratePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(motor2EmptyVolField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(motor2EmptyVolLabel))
                        .addGap(18, 18, 18)
                        .addComponent(motor2FillButton)
                        .addGap(18, 18, 18)
                        .addGroup(callibratePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(motor2FullVolField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(motor2FullVolLabel))
                        .addGap(18, 18, 18)
                        .addGroup(callibratePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(motor2CallibrateButton)
                            .addComponent(motor2WorkingDoneLabel)))
                    .addGroup(callibratePanelLayout.createSequentialGroup()
                        .addComponent(motor1DispenseButton)
                        .addGap(18, 18, 18)
                        .addGroup(callibratePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(motor1EmptyVolField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(motor1EmptyVolLabel))
                        .addGap(18, 18, 18)
                        .addComponent(motor1FillButton)
                        .addGap(18, 18, 18)
                        .addGroup(callibratePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(motor1FullVolField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(motor1FullVolLabel))
                        .addGap(18, 18, 18)
                        .addGroup(callibratePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(motor1CallibrateButton)
                            .addComponent(motor1WorkingDoneLabel))))
                .addContainerGap(319, Short.MAX_VALUE))
        );

        tabPane.addTab("Calibration", callibratePanel);

        toggleTitle.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        toggleTitle.setText("Toggles");

        stirrerLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        stirrerLabel.setText("Stirrer:");

        interiorLightingLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        interiorLightingLabel.setText("Interior Lighting:");

        bubblerLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        bubblerLabel.setText("Bubbler:");

        laserLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        laserLabel.setText("Laser:");

        targetValuesTitle.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        targetValuesTitle.setText("Target Values");

        targetTempLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        targetTempLabel.setText("Temperature:");

        buttonGroup1.add(celciusRB);
        celciusRB.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        celciusRB.setText("C");

        buttonGroup1.add(fahrenheitRB);
        fahrenheitRB.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        fahrenheitRB.setText("F");

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel1.setText("Color:");

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel2.setText("Red");

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel3.setText("Green");

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel4.setText("Blue");

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel5.setText("Turbidity:");

        pumpsLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        pumpsLabel.setText("Pumps");

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel7.setText("Pump 1:");

        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel8.setText("Pump 2:");

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel9.setText("Pump 3:");
        jLabel9.setToolTipText("");

        dispenseLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        dispenseLabel1.setText("Dispense:");

        dispenseLabel2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        dispenseLabel2.setText("Dispense:");

        dispenseLabel3.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        dispenseLabel3.setText("Dispense:");

        mlLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        mlLabel1.setText("ml");

        mlLabel2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        mlLabel2.setText("ml");

        mlLabel3.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        mlLabel3.setText("ml");

        goButton1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        goButton1.setText("Go");

        abortButton2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        abortButton2.setText("Abort");

        abortButton3.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        abortButton3.setText("Abort");

        refillButton2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        refillButton2.setText("Refill");
        refillButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                refillButton2MousePressed(evt);
            }
        });

        refillButton1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        refillButton1.setText("Refill");
        refillButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                refillButton1MousePressed(evt);
            }
        });

        refillButton3.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        refillButton3.setText("Refill");
        refillButton3.setMaximumSize(new java.awt.Dimension(63, 25));
        refillButton3.setMinimumSize(new java.awt.Dimension(63, 25));
        refillButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                refillButton3MousePressed(evt);
            }
        });

        submitButton.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        submitButton.setText("Submit");

        stirrerToggle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        stirrerToggle.setText("On");
        stirrerToggle.setMaximumSize(new java.awt.Dimension(55, 52));
        stirrerToggle.setMinimumSize(new java.awt.Dimension(55, 52));
        stirrerToggle.setPreferredSize(new java.awt.Dimension(55, 52));

        interiorLightingToggle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        interiorLightingToggle.setText("On");

        bubblerToggle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        bubblerToggle.setText("On");

        laserToggle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        laserToggle.setText("On");

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel10.setText("in");

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel11.setText("seconds");

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel12.setText("in");

        jLabel13.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel13.setText("seconds");

        jLabel14.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel14.setText("in");

        jLabel15.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel15.setText("seconds");

        abortButton1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        abortButton1.setText("Abort");

        goButton2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        goButton2.setText("Go");

        goButton3.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        goButton3.setText("Go");
        goButton3.setMaximumSize(new java.awt.Dimension(63, 25));
        goButton3.setMinimumSize(new java.awt.Dimension(63, 25));

        javax.swing.GroupLayout editValuesPanelLayout = new javax.swing.GroupLayout(editValuesPanel);
        editValuesPanel.setLayout(editValuesPanelLayout);
        editValuesPanelLayout.setHorizontalGroup(
            editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editValuesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(editValuesPanelLayout.createSequentialGroup()
                        .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, editValuesPanelLayout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addGap(143, 143, 143))
                            .addGroup(editValuesPanelLayout.createSequentialGroup()
                                .addGap(59, 59, 59)
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel13))
                            .addGroup(editValuesPanelLayout.createSequentialGroup()
                                .addComponent(dispenseLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dispenseField2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mlLabel2)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addGroup(editValuesPanelLayout.createSequentialGroup()
                                .addComponent(dispenseLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dispenseField1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mlLabel1))
                            .addGroup(editValuesPanelLayout.createSequentialGroup()
                                .addGap(62, 62, 62)
                                .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(goButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(editValuesPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel10)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel11))
                                    .addComponent(abortButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(refillButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(64, 64, 64)
                        .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(abortButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(editValuesPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel14)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel15))
                                    .addComponent(refillButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(goButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(editValuesPanelLayout.createSequentialGroup()
                                    .addComponent(dispenseLabel3)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(dispenseField3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(mlLabel3)
                                    .addGap(44, 44, 44))))
                        .addGap(65, 65, 65))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, editValuesPanelLayout.createSequentialGroup()
                        .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(pumpsLabel, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, editValuesPanelLayout.createSequentialGroup()
                                .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(editValuesPanelLayout.createSequentialGroup()
                                        .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(editValuesPanelLayout.createSequentialGroup()
                                                .addComponent(stirrerLabel)
                                                .addGap(53, 53, 53))
                                            .addComponent(interiorLightingLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                                        .addGap(18, 18, 18)
                                        .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(interiorLightingToggle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(stirrerToggle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(46, 46, 46)
                                        .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(editValuesPanelLayout.createSequentialGroup()
                                                .addComponent(laserLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(laserToggle, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(editValuesPanelLayout.createSequentialGroup()
                                                .addComponent(bubblerLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(bubblerToggle, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addComponent(toggleTitle))
                                .addGap(152, 152, 152)
                                .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(editValuesPanelLayout.createSequentialGroup()
                                        .addComponent(targetTempLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(targetTempField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(celciusRB)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(fahrenheitRB))
                                    .addGroup(editValuesPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(targetValuesTitle)
                                    .addGroup(editValuesPanelLayout.createSequentialGroup()
                                        .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(editValuesPanelLayout.createSequentialGroup()
                                                .addComponent(jLabel1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jLabel2)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jLabel3)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jLabel4))
                                            .addGroup(editValuesPanelLayout.createSequentialGroup()
                                                .addGap(98, 98, 98)
                                                .addComponent(submitButton)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(26, 26, 26))))
            .addGroup(editValuesPanelLayout.createSequentialGroup()
                .addGap(69, 69, 69)
                .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(abortButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(goButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(refillButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        editValuesPanelLayout.setVerticalGroup(
            editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editValuesPanelLayout.createSequentialGroup()
                .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(editValuesPanelLayout.createSequentialGroup()
                        .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(editValuesPanelLayout.createSequentialGroup()
                                .addGap(25, 25, 25)
                                .addComponent(toggleTitle)
                                .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(bubblerLabel)
                                            .addComponent(bubblerToggle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(editValuesPanelLayout.createSequentialGroup()
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(stirrerToggle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(stirrerLabel))))
                                    .addGroup(editValuesPanelLayout.createSequentialGroup()
                                        .addGap(52, 52, 52)
                                        .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel1)
                                            .addComponent(jLabel2)
                                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel3)
                                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel4)
                                            .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addGroup(editValuesPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(targetValuesTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(targetTempLabel)
                                    .addComponent(targetTempField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(celciusRB)
                                    .addComponent(fahrenheitRB))))
                        .addGap(27, 27, 27)
                        .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(laserLabel)
                            .addComponent(interiorLightingLabel)
                            .addComponent(laserToggle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(interiorLightingToggle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(editValuesPanelLayout.createSequentialGroup()
                        .addGap(147, 147, 147)
                        .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(submitButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(pumpsLabel)
                .addGap(13, 13, 13)
                .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(editValuesPanelLayout.createSequentialGroup()
                        .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(editValuesPanelLayout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(dispenseLabel2)
                                    .addComponent(dispenseField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(mlLabel2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel12)
                                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel13)))
                            .addGroup(editValuesPanelLayout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(dispenseLabel1)
                                    .addComponent(dispenseField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(mlLabel1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel10)
                                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel11))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(goButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(goButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(editValuesPanelLayout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(dispenseLabel3)
                            .addComponent(dispenseField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mlLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14)
                            .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(goButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)))
                .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(abortButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(abortButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(abortButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(7, 7, 7)
                .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(refillButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(editValuesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(refillButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(refillButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        tabPane.addTab("Control", editValuesPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabPane, javax.swing.GroupLayout.DEFAULT_SIZE, 852, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonExitActionPerformed
        int response = -1;
        response = JOptionPane.showConfirmDialog(this, "Exit Chromastat Control Software?", "Confirm Exit", JOptionPane.YES_NO_OPTION);

        if (response == 0) {
            System.exit(0);
        }
    }//GEN-LAST:event_buttonExitActionPerformed

    private void recordingButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_recordingButtonMouseClicked
        if (isRecording == false) {
            isRecording = true;
            recordingButton.setText("Stop Recording");
        } else {
            recordingButton.setText("Start Recording");
            isRecording = false;
        }
    }//GEN-LAST:event_recordingButtonMouseClicked

    private void refillButton1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refillButton1MousePressed
        pump1.fillCompletely();
    }//GEN-LAST:event_refillButton1MousePressed

    private void refillButton2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refillButton2MousePressed
        pump2.fillCompletely();
    }//GEN-LAST:event_refillButton2MousePressed

    private void refillButton3MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refillButton3MousePressed
        pump3.fillCompletely();
    }//GEN-LAST:event_refillButton3MousePressed

    private void motor1DispenseButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_motor1DispenseButtonMousePressed
        //pump1.dispenseCompletely();
    }//GEN-LAST:event_motor1DispenseButtonMousePressed

    private void motor2DispenseButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_motor2DispenseButtonMousePressed
        //pump2.dispenseCompletely();
    }//GEN-LAST:event_motor2DispenseButtonMousePressed

    private void motor3DispenseButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_motor3DispenseButtonMousePressed
        //pump3.dispenseCompletely();
    }//GEN-LAST:event_motor3DispenseButtonMousePressed

    private void motor1FillButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_motor1FillButtonMousePressed
        //pump1.fillCompletely();
    }//GEN-LAST:event_motor1FillButtonMousePressed

    private void motor2FillButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_motor2FillButtonMousePressed
        //pump2.fillCompletely();
    }//GEN-LAST:event_motor2FillButtonMousePressed

    private void motor3FillButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_motor3FillButtonMousePressed
        //pump3.fillCompletely();
    }//GEN-LAST:event_motor3FillButtonMousePressed

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
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new MainWindow().setVisible(true);
                } catch (IOException | I2CFactory.UnsupportedBusNumberException | InterruptedException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton abortButton1;
    private javax.swing.JButton abortButton2;
    private javax.swing.JButton abortButton3;
    private javax.swing.JLabel brightnessLabel;
    private javax.swing.JPanel brightnessPanel;
    private javax.swing.JLabel bubblerLabel;
    private javax.swing.JToggleButton bubblerToggle;
    private javax.swing.JButton buttonExit;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.ButtonGroup buttonGroup6;
    private javax.swing.JPanel callibratePanel;
    private javax.swing.JRadioButton celciusRB;
    private javax.swing.JPanel colorLabel;
    private javax.swing.JPanel colorPanel;
    private javax.swing.JLabel colorSensorTitleLabel;
    private javax.swing.JLabel colorStringLabel;
    private javax.swing.JTextField dispenseField1;
    private javax.swing.JTextField dispenseField2;
    private javax.swing.JTextField dispenseField3;
    private javax.swing.JLabel dispenseLabel1;
    private javax.swing.JLabel dispenseLabel2;
    private javax.swing.JLabel dispenseLabel3;
    private javax.swing.JPanel editValuesPanel;
    private javax.swing.JRadioButton fahrenheitRB;
    private javax.swing.JLabel fluid1Label;
    private javax.swing.JLabel fluid2Label;
    private javax.swing.JLabel fluid3Label;
    private javax.swing.JButton goButton1;
    private javax.swing.JButton goButton2;
    private javax.swing.JButton goButton3;
    private javax.swing.JLabel hueLabel;
    private javax.swing.JPanel huePanel;
    private javax.swing.JLabel humidityNumLabel;
    private javax.swing.JLabel humidityTilteLabel;
    private javax.swing.JLabel interiorLightingLabel;
    private javax.swing.JToggleButton interiorLightingToggle;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JLabel laserLabel;
    private javax.swing.JToggleButton laserToggle;
    private javax.swing.JLabel lightNumLabel;
    private javax.swing.JLabel lightSensorTitleLabel;
    private javax.swing.JLabel mlLabel1;
    private javax.swing.JLabel mlLabel2;
    private javax.swing.JLabel mlLabel3;
    private javax.swing.JButton motor1CallibrateButton;
    private javax.swing.JButton motor1DispenseButton;
    private javax.swing.JTextField motor1EmptyVolField;
    private javax.swing.JLabel motor1EmptyVolLabel;
    private javax.swing.JButton motor1FillButton;
    private javax.swing.JTextField motor1FullVolField;
    private javax.swing.JLabel motor1FullVolLabel;
    private javax.swing.JLabel motor1WorkingDoneLabel;
    private javax.swing.JButton motor2CallibrateButton;
    private javax.swing.JButton motor2DispenseButton;
    private javax.swing.JTextField motor2EmptyVolField;
    private javax.swing.JLabel motor2EmptyVolLabel;
    private javax.swing.JButton motor2FillButton;
    private javax.swing.JTextField motor2FullVolField;
    private javax.swing.JLabel motor2FullVolLabel;
    private javax.swing.JLabel motor2WorkingDoneLabel;
    private javax.swing.JButton motor3CallibrateButton;
    private javax.swing.JButton motor3DispenseButton;
    private javax.swing.JTextField motor3EmptyVolField;
    private javax.swing.JLabel motor3EmptyVolLabel;
    private javax.swing.JButton motor3FillButton;
    private javax.swing.JTextField motor3FullVolField;
    private javax.swing.JLabel motor3FullVolLabel;
    private javax.swing.JLabel motor3WorkingDoneLabel;
    private javax.swing.JLabel pump1TitleLabel;
    private javax.swing.JLabel pump2TitleLabel;
    private javax.swing.JLabel pump3TitleLabel;
    private javax.swing.JProgressBar pumpBar1;
    private javax.swing.JProgressBar pumpBar2;
    private javax.swing.JProgressBar pumpBar3;
    private javax.swing.JLabel pumpsAllTitleLabel1;
    private javax.swing.JLabel pumpsLabel;
    private javax.swing.JButton recordingButton;
    private javax.swing.JButton refillButton1;
    private javax.swing.JButton refillButton2;
    private javax.swing.JButton refillButton3;
    private javax.swing.JLabel sensorTitle;
    private javax.swing.JLabel stirrerLabel;
    private javax.swing.JToggleButton stirrerToggle;
    private javax.swing.JButton submitButton;
    private javax.swing.JTabbedPane tabPane;
    private javax.swing.JTextField targetTempField;
    private javax.swing.JLabel targetTempLabel;
    private javax.swing.JLabel targetValuesTitle;
    private javax.swing.JLabel tempNumLabel;
    private javax.swing.JLabel tempSensorTitleLabel;
    private javax.swing.JLabel toggleTitle;
    private javax.swing.JLabel turbidityNumLabel;
    private javax.swing.JLabel turbiditySensorTitleLabel;
    private javax.swing.JLabel volume1Label;
    private javax.swing.JLabel volume2Label;
    private javax.swing.JLabel volume3Label;
    // End of variables declaration//GEN-END:variables

}
