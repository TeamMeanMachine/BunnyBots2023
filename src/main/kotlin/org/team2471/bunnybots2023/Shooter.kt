package org.team2471.bunnybots2023

import edu.wpi.first.wpilibj.AnalogInput
import edu.wpi.first.wpilibj.DigitalInput
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.actuators.TalonID
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.Subsystem


object Shooter : Subsystem("Shooter") {
    val shooterMotorOne = MotorController(TalonID(Talons.SHOOTER_ONE), TalonID(Talons.SHOOTER_TWO))
//    val shooterEncoder = AnalogInput(AnalogSensors.SHOOTER_ENCODER)
    val uptakeMotor = MotorController(TalonID(Talons.HOPPER_UPTAKE))
    val uptakeSensor = DigitalInput(DigitalSensors.HOPPER_HIGH)


    var ballReady: Boolean = uptakeSensor.get()
        get() { prevBallReady = field; field = uptakeSensor.get(); return field }
    var prevBallReady = ballReady

    val rpm: Int
        get() = 0
    var rpmSetpoint: Int = 0
        set(value) { field = value }

    init {

    }

    override suspend fun default() {
        periodic {
            if (!ballReady && prevBallReady) {
                uptakeMotor.setPercentOutput(0.1)
            } else if (ballReady && !prevBallReady) {
                uptakeMotor.setPercentOutput(0.0)
            }
        }
    }
}