package org.team2471.off2023

import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.PneumaticsModuleType
import edu.wpi.first.wpilibj.Solenoid
import org.team2471.bunnybots2023.DigitalSensors
import org.team2471.bunnybots2023.Solenoids
import org.team2471.bunnybots2023.Talons
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.actuators.TalonID
import org.team2471.frc.lib.framework.Subsystem

object Intake : Subsystem("Intake") {
    val intakeMotorFront = MotorController(TalonID(Talons.INTAKE_FRONT))
    val intakeMotorLeft = MotorController(TalonID(Talons.INTAKE_LEFT))
    val intakeMotorRight = MotorController(TalonID(Talons.INTAKE_RIGHT))
    val intakeSolenoid = Solenoid(PneumaticsModuleType.REVPH, Solenoids.INTAKE)
    val conveyorMotor = MotorController(TalonID(Talons.HOPPER_CONVEYOR))

    val lowSensor = DigitalInput(DigitalSensors.HOPPER_LOW)


    var prevLowState = lowSensor.get()
    var intaking = false
}