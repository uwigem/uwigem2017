// iGEM Team Washington
// SyringePump controller
// This program allows the user to control a single syringe pump

package syringepump;
import java.lang.*;
import com.pi4j.io.gpio.*;
import com.pi4j.*;

public class SyringePump {
	private GpioController gpio;
	private GpioPinDigitalOutput motor;
	private GpioPinDigitalOutput direction;
	public static final int DEFAULT_SPEED = 1;
	public static final int FLUID_EMPTY = 0;
	public static final int FLUID_FULL = 1000;
	private int outputSpeed;
	private boolean currentlyDispensing;
	private int dispenseGoal;
	private int fluidLevel;
	private String identity;

	public SyringePump(GpioPinDigitalOutput motorPin, String motorName, 
				GpioPinDigitalOutput directionMotor, String directionMotorName, String identifyer) {
		this.outputSpeed = DEFAULT_SPEED;
		this.currentlyDispensing = false;
		this.dispenseGoal = 0;
		this.fluidLevel = FLUID_EMPTY;
                this.gpio = GpioFactory.getInstance();
		this.motor = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_26, motorName);
		this.direction = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, directionMotorName);
		this.direction.high(); // Sets direction to be sucking in initially
		this.initialize();
		this.identity = identifyer.toUpperCase();
	} 

	private void initialize() {
		System.out.println("Initializing pump [" + this.identity + "]-- Please wait");
		this.outputSpeed = DEFAULT_SPEED;
		this.direction.high();
		this.fluidLevel = FLUID_EMPTY;
		/*for(int i = 0; i < FLUID_FULL; i++) {
			Thread.sleep(this.outputSpeed);
			motor.high();
			fluidLevel+= 1;
			Thread.sleep(this.outputSpeed);
			motor.low();
		}*/
		System.out.println("Pump [" + this.identity + "] completed initialization");
	}

	private boolean insideThreshold() {
		return this.fluidLevel >= FLUID_EMPTY && this.fluidLevel <= FLUID_FULL;
	}

	public void dispense() { //positive input, dispenses initialgoal amount, subtracts from full
		this.direction.low();
/*
		//redo this math
		if(!insideThreshold(this.fluidLevel - initialGoal)) { // if not in threshold, refill to max.   Check out the math on this
			if(!this.refill(initialGoal)) {
				return false;
			}
		}

		if(this.checkLegal()) {
			this.currentlyDispensing = true;
			for(int i = 0; i < this.dispenseGoal; i++) {
				if(insideThreshold(fluidLevel)) {
					Thread.sleep(this.outputSpeed);
					motor.high();
					fluidLevel+= -1;
					Thread.sleep(this.outputSpeed);
					motor.low();
				} else {
					System.out.println("Error. Threshold reached-- this should never happen");
				}
			}
			this.currentlyDispensing = false;
		}*/
	}

        /*
	private boolean checkLegal() {
            
	}*/

	public boolean isCurrentlyDispensing() {
		return this.currentlyDispensing;
	}

	public void setGoal(int newGoal) {
		if(newGoal < FLUID_EMPTY || newGoal > 0) {
			throw new IllegalArgumentException("Cannot dispense negative amount");
		}
		this.dispenseGoal = newGoal;
	}

	public void setSpeed(int newSpeed) { // doesn't allow a or negative 0 speed
		if(newSpeed <= 0) {
			this.outputSpeed = DEFAULT_SPEED;
		}
		this.outputSpeed = newSpeed;
	}

	public void stopAll() {
		this.dispenseGoal = 0;
	}

	public int getGoal() {
		return this.dispenseGoal;
	}

	public int getSpeed() { // change this to mL/s or μm/s
		return this.outputSpeed;
	}

	public void manualReset() { // Maybe use the slip tube on the motor for this?
		this.initialize();
	}

	private void updateFluidLevel(int steps) {
		this.fluidLevel += steps;
	}

	private void refill() { // if requriedamt greater, then Error
		/*for(int i = fluidLevel; i < FLUID_FULL; i++) {
			Thread.sleep(this.outputSpeed);
			motor.high();
			fluidLevel+= 1;
			Thread.sleep(this.outputSpeed);
			motor.low();
		}*/
	}
}