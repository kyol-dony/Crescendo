// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.FunctionalCommand;
import frc.robot.subsystems.LEDController;
import frc.robot.subsystems.LEDController.LEDColor;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private Command m_autonomousCommand;

  private RobotContainer m_robotContainer;
  private LEDController m_ledController;
  private enum LedState { IDLE, LIMIT_SWITCH, SHOOT, INTAKE }


  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    // Instantiate our RobotContainer.  This will perform all our button bindings, and put our
    // autonomous chooser on the dashboard.
    m_robotContainer = new RobotContainer();
    m_ledController = m_robotContainer.getLEDController();
  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    // Runs the Scheduler.  This is responsible for polling buttons, adding newly-scheduled
    // commands, running already-scheduled commands, removing finished or interrupted commands,
    // and running subsystem periodic() methods.  This must be called from the robot's periodic
    // block in order for anything in the Command-based framework to work.
    CommandScheduler.getInstance().run();
      
    }

  /** This function is called once each time the robot enters Disabled mode. */
  @Override
  public void disabledInit() {
    m_ledController.applyColorSolid(LEDController.LEDColor.TR_BLUE);
    m_ledController.startSnakeAnimation(LEDColor.TR_BLUE, LEDColor.TR_RED, true);
    // m_ledController.startSnakeAnimation(LEDColor.TR_BLUE, LEDColor.TR_RED, true);
  }

  @Override
  public void disabledPeriodic() {}

  /** This autonomous runs the autonomous command selected by your {@link RobotContainer} class. */
  @Override
  public void autonomousInit() {
    m_autonomousCommand = m_robotContainer.getAutonomousCommand();

    // schedule the autonomous command (example)
    if (m_autonomousCommand != null) {
      m_autonomousCommand.schedule();
    }
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {}

  @Override
  public void teleopInit() {
    // This makes sure that the autonomous stops running when
    // teleop starts running. If you want the autonomous to
    // continue until interrupted by another command, remove
    // this line or comment it out.
    if (m_autonomousCommand != null) {
      m_autonomousCommand.cancel();
    }
    m_robotContainer.teleopInitRoutine();
    final LedState[] currentState = new LedState[] {LedState.IDLE};
    m_ledController.setDefaultCommand(new FunctionalCommand(
        () -> {
          currentState[0] = LedState.IDLE;
          m_ledController.stopLimitSwitchProgressLoop();
          m_ledController.applyBlinkColor(LEDColor.TR_RED);
        },
        () -> {
          boolean limitSwitchBroken = m_robotContainer.isIntakeLimitSwitchTriggered();
          boolean intakeActive = m_robotContainer.isIntaking();
          boolean shooterRevving = m_robotContainer.isShooterRevving();

          LedState desiredState = LedState.IDLE;
          if (intakeActive) {
            desiredState = LedState.INTAKE;
          } else if (shooterRevving) {
            desiredState = LedState.SHOOT;
          } else if (limitSwitchBroken) {
            desiredState = LedState.LIMIT_SWITCH;
          }

          if (desiredState != currentState[0]) {
            if (currentState[0] == LedState.LIMIT_SWITCH) {
              m_ledController.stopLimitSwitchProgressLoop();
            }

            switch (desiredState) {
              case INTAKE:
                m_ledController.applyColorBlink(LEDColor.PURPLE, LEDColor.OFF, 0);
                break;
              case SHOOT:
                m_ledController.applyColorBlink(LEDColor.GREEN, LEDColor.OFF, 0);
                break;
              case LIMIT_SWITCH:
                m_ledController.startLimitSwitchProgressLoop();
                break;
              case IDLE:
              default:
                m_ledController.applyBlinkColor(LEDColor.TR_RED);
                break;
            }
            currentState[0] = desiredState;
          }

          if (currentState[0] == LedState.LIMIT_SWITCH) {
            m_ledController.runLimitSwitchProgressLoop();
          }
        },
        interrupted -> {
          currentState[0] = LedState.IDLE;
          m_ledController.stopLimitSwitchProgressLoop();
        },
        () -> false,
        m_ledController));
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {}

  @Override
  public void testInit() {
    // Cancels all running commands at the start of test mode.
    CommandScheduler.getInstance().cancelAll();
  }

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {
  }
}

