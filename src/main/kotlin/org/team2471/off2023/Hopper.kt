package org.team2471.off2023

import org.team2471.bunnybots2023.Talons
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.actuators.TalonID

object Hopper {
    val conveyorMotor = MotorController(TalonID(Talons.HOPPER_CONVEYOR))
}