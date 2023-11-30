package org.team2471.bunnybots2023

import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.PneumaticsModuleType
import edu.wpi.first.wpilibj.Solenoid
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.actuators.TalonID
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.Subsystem

object Intake : Subsystem("Intake") {
    val table = NetworkTableInstance.getDefault().getTable("Intake")

    val isDownEntry = table.getEntry("isDown")
    val frontPowerEntry = table.getEntry("Front Power")
    val centerPowerEntry = table.getEntry("Center Power")
    val frontMotorCurrentEntry = table.getEntry("Front Current")
    val centerMotorCurrentEntry = table.getEntry("Center Current")
    val intakingEntry = table.getEntry("Intaking")
    val ballLoadedEntry = table.getEntry("Ball Loaded")
    val disableConveyorEntry = table.getEntry("Disabled Conveyor")


    val frontMotor = MotorController(TalonID(Talons.INTAKE_FRONT))
    val centerMotor = MotorController(TalonID(Talons.INTAKE_CENTER))
    val solenoid = Solenoid(PneumaticsModuleType.REVPH, Solenoids.INTAKE)
    val conveyorMotor = MotorController(TalonID(Talons.HOPPER_CONVEYOR))
    val lowSensor = DigitalInput(DigitalSensors.HOPPER_LOW)

    var ballLoaded = lowSensor.get()
        get() { prevBallLoaded = field; field = lowSensor.get(); return field }
    var prevBallLoaded = ballLoaded

    val intaking: Boolean
        get() = frontMotor.current > 1.0
    val isDown = solenoid.get()

    val frontPower: Double
        get() = frontPowerEntry.getDouble(1.0).coerceIn(0.0, 1.0)
    val centerPower: Double
        get() = centerPowerEntry.getDouble(1.0).coerceIn(0.0, 1.0)

    var disableConveyor = false




    init {

        frontMotor.config {
           currentLimit(30, 40,0)
           inverted(true)
//           coastMode()
        }
        centerMotor.config {
            currentLimit(30, 40, 0)
//            coastMode()
        }
        conveyorMotor.config {
            currentLimit(30, 40, 0)
            brakeMode()
        }

        if (!frontPowerEntry.exists()) {
           frontPowerEntry.setDouble(1.0)
           frontPowerEntry.setPersistent()
        }
        if (!centerPowerEntry.exists()) {
            centerPowerEntry.setDouble(1.0)
            centerPowerEntry.setPersistent()
        }

        GlobalScope.launch {
            periodic {
                isDownEntry.setBoolean(isDown)
                frontMotorCurrentEntry.setDouble(frontMotor.current)
                centerMotorCurrentEntry.setDouble(centerMotor.current)
                intakingEntry.setBoolean(intaking)
                ballLoadedEntry.setBoolean(ballLoaded)
                disableConveyorEntry.setBoolean(disableConveyor)
//                println("low ${lowSensor.get()}")
            }
        }
    }

    fun startIntake() {
        frontMotor.setPercentOutput(frontPower)
        centerMotor.setPercentOutput(centerPower)
        println("Intake starting")
        OI.driverController.rumble = 0.5
//        intaking = true
    }
    fun stopIntake() {
        frontMotor.setPercentOutput(0.0)
        centerMotor.setPercentOutput(0.0)
        println("Intake stopping")
        OI.driverController.rumble = 0.0
//        intaking = false
    }

    fun toggleIntake() {
        if (intaking) {
            stopIntake()
        } else {
            startIntake()
        }
    }

    fun intakeDown() {
        solenoid.set(true)
    }
    fun intakeUp() {
        solenoid.set(false)
    }

    override fun preEnable() {
        stopIntake()
        conveyorMotor.setPercentOutput(0.0)
    }
    override fun onDisable() {
        stopIntake()
        conveyorMotor.setPercentOutput(0.0)
    }

    override suspend fun default() {
        var reverseBall = false
        var detectedBall = false
        periodic(period = 0.001) {
            if (!disableConveyor) {
                if (ballLoaded && !prevBallLoaded) {
                    println("detected a ball!!")
                    conveyorMotor.setPercentOutput(0.0)
                    reverseBall = true
                } else if (!ballLoaded) {
                    conveyorMotor.setPercentOutput(1.0)
                    detectedBall = false
                }
            } else {
                conveyorMotor.setPercentOutput(0.0)
            }
        }
    }
}