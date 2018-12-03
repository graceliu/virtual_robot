package virtual_robot.controller;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import virtual_robot.hardware.DCMotor;
import virtual_robot.hardware.HardwareMap;

public class TwoWheelBot extends VirtualBot {

    private VirtualRobotController.DCMotorImpl leftMotor = null;
    private VirtualRobotController.DCMotorImpl rightMotor = null;
    private VirtualRobotController.GyroSensorImpl gyro = null;
    private VirtualRobotController.ColorSensorImpl colorSensor = null;
    private VirtualRobotController.ServoImpl servo = null;

    private Rectangle backServoArm = null;
    private double wheelCircumference;
    private double interWheelDistance;



    public TwoWheelBot(HardwareMap hwMap, double fieldWidth, StackPane fieldPane){
        super(hwMap, fieldWidth);
        leftMotor = hwMap.dcMotor.get("left_motor");
        rightMotor = hwMap.dcMotor.get("right_motor");
        gyro = hwMap.gyroSensor.get("gyro_sensor");
        colorSensor = hwMap.colorSensor.get("color_sensor");
        servo = hwMap.servo.get("back_servo");
        wheelCircumference = Math.PI * botWidth / 4.5;
        interWheelDistance = botWidth * 8.0 / 9.0;
        setUpDisplayGroup("two_wheel_bot.fxml", fieldPane);
        backServoArm = (Rectangle)displayGroup.getChildren().get(5);
        backServoArm.getTransforms().add(new Rotate(0, 37.5, 67.5));
    }

    public synchronized void updateStateAndSensors(double millis){
        double leftTicks = leftMotor.getCurrentPositionDouble();
        double rightTicks = rightMotor.getCurrentPositionDouble();
        leftMotor.updatePosition(millis);
        rightMotor.updatePosition(millis);
        double newLeftTicks = leftMotor.getCurrentPositionDouble();
        double newRightTicks = rightMotor.getCurrentPositionDouble();
        double intervalLeftTicks = newLeftTicks - leftTicks;
        double intervalRightTicks = newRightTicks - rightTicks;
        double leftWheelDist = intervalLeftTicks * wheelCircumference / VirtualRobotController.DCMotorImpl.TICKS_PER_ROTATION;
        if (leftMotor.getDirection() == DCMotor.Direction.FORWARD) leftWheelDist = -leftWheelDist;
        double rightWheelDist = intervalRightTicks * wheelCircumference / VirtualRobotController.DCMotorImpl.TICKS_PER_ROTATION;
        if (rightMotor.getDirection() == DCMotor.Direction.REVERSE) rightWheelDist = -rightWheelDist;
        double distTraveled = (leftWheelDist + rightWheelDist) / 2.0;
        double headingChange = (rightWheelDist - leftWheelDist) / interWheelDistance;
        double deltaRobotX = -distTraveled * Math.sin(headingRadians + headingChange / 2.0);
        double deltaRobotY = distTraveled * Math.cos(headingRadians + headingChange / 2.0);
        x += deltaRobotX;
        y += deltaRobotY;
        if (x >  (halfFieldWidth - halfBotWidth)) x = halfFieldWidth - halfBotWidth;
        else if (x < (halfBotWidth - halfFieldWidth)) x = halfBotWidth - halfFieldWidth;
        if (y > (halfFieldWidth - halfBotWidth)) y = halfFieldWidth - halfBotWidth;
        else if (y < (halfBotWidth - halfFieldWidth)) y = halfBotWidth - halfFieldWidth;
        headingRadians += headingChange;
        if (headingRadians > Math.PI) headingRadians -= 2.0 * Math.PI;
        else if (headingRadians < -Math.PI) headingRadians += 2.0 * Math.PI;
        gyro.updateHeading(headingRadians * 180.0 / Math.PI);
        colorSensor.updateColor(x, y);
    }

    public synchronized void updateDisplay(){
        super.updateDisplay();
        ((Rotate)backServoArm.getTransforms().get(0)).setAngle(-180.0 * servo.getPosition());
    }

    public void powerDownAndReset(){
        leftMotor.setPower(0);
        rightMotor.setPower(0);
        gyro.deinit();
    }


}
