package org.team2471.off2023

import edu.wpi.first.wpilibj.AnalogInput
import org.team2471.bunnybots2023.AnalogSensors
import org.team2471.bunnybots2023.Falcons
import org.team2471.frc.lib.actuators.FalconID
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.degrees

object Turret : Subsystem("Turret") {
    val turningMotorOne = MotorController(FalconID(Falcons.TURRET_ONE))
    val turningMotorTwo = MotorController(FalconID(Falcons.TURRET_TWO))
    val turretEncoder = AnalogInput(AnalogSensors.TURRET_ENCODER)

    val turretAngle: Angle
        get() = 0.0.degrees

    var turretSetpoint = null

}