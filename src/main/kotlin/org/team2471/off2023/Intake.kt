package org.team2471.off2023

import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.PneumaticsModuleType
import edu.wpi.first.wpilibj.Solenoid
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.bunnybots2023.DigitalSensors
import org.team2471.bunnybots2023.Solenoids
import org.team2471.bunnybots2023.Talons
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.actuators.TalonID
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.Subsystem

object Intake : Subsystem("Intake") {
    val table = NetworkTableInstance.getDefault().getTable("Intake")

    val isDownEntry = table.getEntry("isDown")


    val frontMotor = MotorController(TalonID(Talons.INTAKE_FRONT))
    val centerMotor = MotorController(TalonID(Talons.INTAKE_CENTER))
    val solenoid = Solenoid(PneumaticsModuleType.REVPH, Solenoids.INTAKE)
    val conveyorMotor = MotorController(TalonID(Talons.HOPPER_CONVEYOR))

    val lowSensor = DigitalInput(DigitalSensors.HOPPER_LOW)


    var prevLowState = lowSensor.get()
    val intaking: Boolean
        get() = frontMotor.current > 1.0
    val isDown = solenoid.get()



    init {

        frontMotor.config {
           currentLimit(0, 40,0)
            coastMode()
        }
        centerMotor.config {
           currentLimit(0, 40,0)
            coastMode()
        }
        GlobalScope.launch {
            periodic {
                isDownEntry.setBoolean(isDown)
            }
        }
    }
    fun toggleIntake() {
        if (intaking) {
            frontMotor.setPercentOutput(0.0)
            centerMotor.setPercentOutput(0.0)

        } else if (isDown) {
            frontMotor.setPercentOutput(1.0)
            centerMotor.setPercentOutput(1.0)
        }
    }

    override fun preEnable() {
        frontMotor.setPercentOutput(0.0)
        centerMotor.setPercentOutput(0.0)
        conveyorMotor.setPercentOutput(0.0)
    }




    fun intakeDown() {
        solenoid.set(true)
    }
    fun intakeUp() {
        solenoid.set(false)
    }



}