// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.Constants.OperatorConstants;
import frc.robot.commands.swervedrive.drivebase.AbsoluteDriveAdv;
import frc.robot.subsystems.swervedrive.SwerveSubsystem;
import java.io.File;

import com.pathplanner.lib.auto.NamedCommands;

import frc.robot.subsystems.Pneumatics;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a "declarative" paradigm, very
 * little robot logic should actually be handled in the {@link Robot} periodic methods (other than the scheduler calls).
 * Instead, the structure of the robot (including subsystems, commands, and trigger mappings) should be declared here.
 */
public class RobotContainer
{

  // Replace with CommandPS4Controller or CommandJoystick if needed
  final CommandXboxController driverXbox = new CommandXboxController(0);
  // The robot's subsystems and commands are defined here...
  private final Pneumatics pneumatics = new Pneumatics();
  private final SwerveSubsystem drivebase = new SwerveSubsystem(new File(Filesystem.getDeployDirectory(),
                                                                         "swerve/bionic-beef-NERD-event"));

  /**
   * The container for the robot. Contains subsystems, OI devices, and commands.
   */
  public RobotContainer()
  {
    // Configure the trigger bindings
    configureBindings();
    registerNamedCommands();

    // Applies deadbands and inverts controls because joysticks
    // are back-right positive while robot
    // controls are front-left positive
    // left stick controls translation
    // right stick controls the rotational velocity 
    // buttons are quick rotation positions to different ways to face
    // WARNING: default buttons are on the same buttons as the ones defined in configureBindings
    AbsoluteDriveAdv closedAbsoluteDriveAdv = new AbsoluteDriveAdv(drivebase,
                                                                   () -> -MathUtil.applyDeadband(driverXbox.getLeftY(),
                                                                                                 OperatorConstants.LEFT_Y_DEADBAND),
                                                                   () -> -MathUtil.applyDeadband(driverXbox.getLeftX(),
                                                                                                 OperatorConstants.LEFT_X_DEADBAND),
                                                                   () -> -MathUtil.applyDeadband(driverXbox.getRightX(),
                                                                                                 OperatorConstants.RIGHT_X_DEADBAND),
                                                                   driverXbox.getHID()::getYButtonPressed,
                                                                   driverXbox.getHID()::getAButtonPressed,
                                                                   driverXbox.getHID()::getXButtonPressed,
                                                                   driverXbox.getHID()::getBButtonPressed);

    // Applies deadbands and inverts controls because joysticks
    // are back-right positive while robot
    // controls are front-left positive
    // left stick controls translation
    // right stick controls the desired angle NOT angular rotation
    Command driveFieldOrientedDirectAngle = drivebase.driveCommand(
        () -> MathUtil.applyDeadband(driverXbox.getLeftY(), OperatorConstants.LEFT_Y_DEADBAND),
        () -> MathUtil.applyDeadband(driverXbox.getLeftX(), OperatorConstants.LEFT_X_DEADBAND),
        () -> driverXbox.getRightX(),
        () -> driverXbox.getRightY());

    // Applies deadbands and inverts controls because joysticks
    // are back-right positive while robot
    // controls are front-left positive
    // left stick controls translation
    // right stick controls the angular velocity of the robot
    // front shoulder buttons slowly rotate the robot
    Command driveFieldOrientedAnglularVelocity = drivebase.driveCommand(
        () -> MathUtil.applyDeadband(driverXbox.getLeftY(), OperatorConstants.LEFT_Y_DEADBAND),
        () -> MathUtil.applyDeadband(driverXbox.getLeftX(), OperatorConstants.LEFT_X_DEADBAND),
        () -> -driverXbox.getRightX() +
              (driverXbox.button(5).getAsBoolean() ? 0.4 : 0) + 
              (driverXbox.button(6).getAsBoolean() ? -0.4 : 0));

    

    Command driveFieldOrientedDirectAngleSim = drivebase.simDriveCommand(
        () -> MathUtil.applyDeadband(driverXbox.getLeftY(), OperatorConstants.LEFT_Y_DEADBAND),
        () -> MathUtil.applyDeadband(driverXbox.getLeftX(), OperatorConstants.LEFT_X_DEADBAND),
        () -> driverXbox.getRawAxis(4));

    drivebase.setDefaultCommand(
        !RobotBase.isSimulation() || false ? driveFieldOrientedDirectAngle : driveFieldOrientedDirectAngleSim);

    drivebase.setDefaultCommand(driveFieldOrientedAnglularVelocity);    
  }


  /**
   * Use this method to define your trigger->command mappings. Triggers can be created via the
   * {@link Trigger#Trigger(java.util.function.BooleanSupplier)} constructor with an arbitrary predicate, or via the
   * named factories in {@link edu.wpi.first.wpilibj2.command.button.CommandGenericHID}'s subclasses for
   * {@link CommandXboxController Xbox}/{@link edu.wpi.first.wpilibj2.command.button.CommandPS4Controller PS4}
   * controllers or {@link edu.wpi.first.wpilibj2.command.button.CommandJoystick Flight joysticks}.
   */
  private void registerNamedCommands() {
    NamedCommands.registerCommand("forwardSpatula", new InstantCommand(() -> pneumatics.forwardSpatulaSolenoid()));
    NamedCommands.registerCommand("reverseSpatula", new InstantCommand(() -> pneumatics.reverseSpatulaSolenoid()));
  }

  private void configureBindings()
  {
    // Schedule `ExampleCommand` when `exampleCondition` changes to `true`

    driverXbox.b().onTrue((Commands.runOnce(drivebase::zeroGyro)));
    //driverXbox.x().onTrue(Commands.runOnce(drivebase::addFakeVisionReading));
    /*driverXbox.b().whileTrue(
        Commands.deferredProxy(() -> drivebase.driveToPose(
                                   new Pose2d(new Translation2d(4, 4), Rotation2d.fromDegrees(0)))
                              ));
                              */
    //driverXbox.y().whileTrue(drivebase.aimAtSpeaker(2));
    driverXbox.y().onTrue(new InstantCommand(() -> pneumatics.forwardLiftSolenoid()));
    driverXbox.y().onFalse(new InstantCommand(() -> pneumatics.reverseLiftSolenoid()));
    driverXbox.a().onTrue(new InstantCommand(() -> pneumatics.forwardSpatulaSolenoid()));
    driverXbox.a().onFalse(new InstantCommand(() -> pneumatics.reverseSpatulaSolenoid()));
    driverXbox.x().whileTrue(Commands.runOnce(drivebase::lock, drivebase).repeatedly());

    driverXbox.povUp().whileTrue(drivebase.drivePOV(0, -1, () -> ((driverXbox.button(5).getAsBoolean() ? 0.1 : 0) + (driverXbox.button(6).getAsBoolean() ? -0.1 : 0))));
    driverXbox.povUpLeft().whileTrue(drivebase.drivePOV(1, -1, () -> ((driverXbox.button(5).getAsBoolean() ? 0.1 : 0) + (driverXbox.button(6).getAsBoolean() ? -0.1 : 0))));
    driverXbox.povUpRight().whileTrue(drivebase.drivePOV(-1, -1, () -> ((driverXbox.button(5).getAsBoolean() ? 0.1 : 0) + (driverXbox.button(6).getAsBoolean() ? -0.1 : 0))));
    driverXbox.povDown().whileTrue(drivebase.drivePOV(0, 1, () -> ((driverXbox.button(5).getAsBoolean() ? 0.1 : 0) + (driverXbox.button(6).getAsBoolean() ? -0.1 : 0))));
    driverXbox.povDownLeft().whileTrue(drivebase.drivePOV(1, 1, () -> ((driverXbox.button(5).getAsBoolean() ? 0.1 : 0) + (driverXbox.button(6).getAsBoolean() ? -0.1 : 0))));
    driverXbox.povDownRight().whileTrue(drivebase.drivePOV(-1, 1, () -> ((driverXbox.button(5).getAsBoolean() ? 0.1 : 0) + (driverXbox.button(6).getAsBoolean() ? -0.1 : 0))));
    driverXbox.povLeft().whileTrue(drivebase.drivePOV(1, 0, () -> ((driverXbox.button(5).getAsBoolean() ? 0.1 : 0) + (driverXbox.button(6).getAsBoolean() ? -0.1 : 0))));
    driverXbox.povRight().whileTrue(drivebase.drivePOV(-1, 0, () -> ((driverXbox.button(5).getAsBoolean() ? 0.1 : 0) + (driverXbox.button(6).getAsBoolean() ? -0.1 : 0))));
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand()
  {
    // An example command will be run in autonomous
    return drivebase.getAutonomousCommand("Blue Alliance Auto");
    // return drivebase.getAutonomousCommand("3 meters ahead Auto");
    // return drivebase.getAutonomousCommand("3 meters ahead-no preset Auto");
    // return drivebase.getAutonomousCommand("Spoil Midline Auto");
  }

  public void setDriveMode()
  {
    //drivebase.setDefaultCommand();
  }

  public void setMotorBrake(boolean brake)
  {
    drivebase.setMotorBrake(brake);
  }
}
