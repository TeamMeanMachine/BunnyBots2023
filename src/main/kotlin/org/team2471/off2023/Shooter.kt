package org.team2471.off2023

import edu.wpi.first.wpilibj.AnalogInput
import org.team2471.bunnybots2023.AnalogSensors
import org.team2471.bunnybots2023.Talons
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.actuators.TalonID


object Shooter {
    val shooterMotorOne = MotorController(TalonID(Talons.SHOOTER_ONE))
    val shooterMotorTwo = MotorController(TalonID(Talons.SHOOTER_TWO))
    val shooterEncoder = AnalogInput(AnalogSensors.SHOOTER_ENCODER)

    val rpm: Int
        get() = 0
    var rpmSetpoint: Int = 0
        set(value) { field = value }
}