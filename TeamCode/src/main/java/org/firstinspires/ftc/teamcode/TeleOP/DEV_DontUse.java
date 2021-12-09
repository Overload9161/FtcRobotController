/*
 * This class is our development piece for our robot
 * TODO: Test now way of moving the robot relative to the field rather than the robot
 * TODO: Develop math to get the position of the robot during TeleOp
 *  	TODO: Cont... Use the IMU or Encoders
 * TODO: Develop and test new FileManager
 * TODO: Develop and test new LEDController
 */


package org.firstinspires.ftc.teamcode.TeleOP;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.util.Threads.File.FileManager;
import org.firstinspires.ftc.teamcode.util.hardware.util.Hardware;
import org.firstinspires.ftc.teamcode.util.hardware.LEDController;
import org.firstinspires.ftc.teamcode.util.hardware.LEDController.state;

@TeleOp(name = "DEV_DontUse", group = "Test")
@Disabled
public class DEV_DontUse extends OpMode {
	
	/*
	 * What the math needs to look like:
	 * 1. Get the angle of the gamepad stick
	 * 2. Get the angle we are relative to the field
	 * 3. Do some math to find the angle of the robot we must
	 * 		move to move in the direction
	 * 4. Calculate the power of each motor
	 */
	
	boolean inReverse 	= false;//reverse button is b
	boolean bWasPressed = false;
	
	Hardware 	  r 			= new Hardware();
	FileManager   fileManager 	= new FileManager(this);
	LEDController ledController = new LEDController(this);
	
	ElapsedTime time;
	
	@Override
	public void init() {
		ledController.setState(state.INIT);
		time.startTime();
		r.initRobot(this);
		this.fileManager.StartTeleOp(time);
		ledController.setState(state.WAIT);
	}
	
	@Override
	public void start() {
		ledController.setState(state.RUNNING);
		super.start();
	}
	
	@Override
	public void loop() {
		//int speed = 0;
		double deflator;
		
		//this code determines what percentage of the motor power that will be used.
		if (gamepad1.right_bumper) {
			deflator = .4;
		} else {
			deflator = .9;
		}
		
		if (gamepad1.left_bumper)
			deflator = 1;
		
		//legacy code that runs our mecanum drive wheels in any direction we want
		
		//this first section creates the variables that will be used later
		
		if (gamepad1.b && !bWasPressed)
			inReverse = !inReverse;
		bWasPressed = gamepad1.b;
		//first we must translate the rectangular values of the joystick into polar coordinates;
		double y = -1 * gamepad1.left_stick_y;
		double x = gamepad1.left_stick_x;
		double angle = 0;
		
		if (y > 0 && x > 0)//quadrant 1
			angle = Math.atan(y / x);
		else {
			double angle1 = Math.toRadians(180) + Math.atan(y / x);
			if (y > 0 && x < 0)//quadrant 2
				angle = angle1;
			else if (y < 0 && x < 0)//quadrant 3
				angle = angle1;
			else if (y < 0 && x > 0)//quadrant 4
				angle = Math.toRadians(360) + Math.atan(y / x);
		}
		
		if (y == 0 && x > 1)
			angle = 0;
		if (y > 0 && x == 0)
			angle = Math.PI / 2;
		if (y == 0 && x < 0)
			angle = Math.PI;
		if (y < 0 && x == 0)
			angle = 3 * Math.PI / 2;
		
		double velocity = Math.sqrt(Math.pow(gamepad1.left_stick_y, 2) + Math.pow(gamepad1.left_stick_x, 2));
		double rotation = gamepad1.right_stick_x;
		
		if (inReverse)//reverse button
			angle += Math.toRadians(180);
		
		angle += Math.toRadians(270);
		
		//equations taking the polar coordinates and turing them into motor powers
		double v1 	  = velocity * Math.cos(angle + (Math.PI / 4));
		double v2 	  = velocity * Math.sin(angle + (Math.PI / 4));
		double power1 = v1 - rotation;
		double power2 = v2 + rotation;
		double power3 = v2 - rotation;
		double power4 = v1 + rotation;
		fileManager.writeFile("Gamepad1-LStick?Polar", new double[]{angle, velocity, rotation}, time.milliseconds());
		fileManager.writeFile("Robot-Power", new double[]{power1, power2, power3, power4}, time.milliseconds());
		r.frontLeft .setPower  (power1 * deflator);
		r.frontRight.setPower  (power2 * deflator);
		r.backLeft  .setPower  (power3 * deflator);
		r.backRight .setPower  (power4 * deflator);
	}
}