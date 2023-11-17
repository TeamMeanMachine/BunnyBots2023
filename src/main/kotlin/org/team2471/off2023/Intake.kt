package org.team2471.off2023

import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.PneumaticsModuleType
import edu.wpi.first.wpilibj.Solenoid
import org.team2471.bunnybots2023.DigitalSensors
import org.team2471.bunnybots2023.Solenoids
import org.team2471.bunnybots2023.Talons
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.actuators.TalonID
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.Subsystem

object Intake : Subsystem("Intake") {
    val intakeMotorFront = MotorController(TalonID(Talons.INTAKE_FRONT))
    val intakeMotorLeft = MotorController(TalonID(Talons.INTAKE_LEFT))
    val intakeMotorRight = MotorController(TalonID(Talons.INTAKE_RIGHT))
    val intakeSolenoid = Solenoid(PneumaticsModuleType.CTREPCM, Solenoids.INTAKE)
    val conveyorMotor = MotorController(TalonID(Talons.HOPPER_CONVEYOR))

    val lowSensor = DigitalInput(DigitalSensors.HOPPER_LOW)
    var intaking = false

    var ballLoaded = lowSensor.get()
        get() { prevBallLoaded = field; field = lowSensor.get(); return field }
    var prevBallLoaded = ballLoaded



    override suspend fun default() {
        periodic {
            if (Shooter.ballReady && ballLoaded) {
                conveyorMotor.setPercentOutput(0.0)
            } else {
                conveyorMotor.setPercentOutput(0.5)
            }
        }

    }
}