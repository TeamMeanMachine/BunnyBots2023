package org.team2471.bunnybots2023

import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.AnalogInput
import edu.wpi.first.wpilibj.DigitalInput
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.actuators.TalonID
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.util.Timer


object Shooter : Subsystem("Shooter") {
    val table = NetworkTableInstance.getDefault().getTable("Shooter")

    val ballReadyEntry = table.getEntry("Ball Ready")
    val uptakeCurrentEntry = table.getEntry("Uptake Current")
    val disableUptakeEntry = table.getEntry("Disabled Uptake")
    val shooterOneCurrentEntry = table.getEntry("Shooter One Current")
    val shooterTwoCurrentEntry = table.getEntry("Shooter Two Current")
    val rpmEntry = table.getEntry("RPM")
    val rpmSetpointEntry = table.getEntry("RPM Setpoint")
    val shooterIdleEntry = table.getEntry("Shooter Idle Power")


    val shooterMotorOne = MotorController(TalonID(Talons.SHOOTER_ONE))
    val shooterMotorTwo = MotorController(TalonID(Talons.SHOOTER_TWO))
//    val shooterEncoder = AnalogInput(AnalogSensors.SHOOTER_ENCODER)
    val uptakeMotor = MotorController(TalonID(Talons.HOPPER_UPTAKE))
    val uptakeSensor = DigitalInput(DigitalSensors.HOPPER_HIGH)


    var ballReady: Boolean = !uptakeSensor.get()
        get() { /*prevBallReady = field*/; field = !uptakeSensor.get(); return field }
//    var prevBallReady = ballReady

    var disableUptake = false
    var detectedBall = false
    var reverseBall = false


    val rpm: Double
        get() = 0.0
    var rpmSetpoint: Double = 0.0
        set(value) { field = value }
    val shooterIdlePower: Double
        get() = shooterIdleEntry.getDouble(0.8).coerceIn(0.0, 1.0)

    init {

        uptakeMotor.config {
            currentLimit(30, 40, 0)
            inverted(true)
            brakeMode()
        }
        shooterMotorTwo.config {
            currentLimit(30, 40, 0)
            coastMode()
        }
        shooterMotorOne.config {
            currentLimit(30, 40, 0)
            inverted(true)
            coastMode()
        }
        if (!shooterIdleEntry.exists()) {
            shooterIdleEntry.setPersistent()
            shooterIdleEntry.setDouble(0.5)
        }


        GlobalScope.launch {
            periodic {
//                println("high ${uptakeSensor.get()}")
                ballReadyEntry.setBoolean(ballReady)
                uptakeCurrentEntry.setDouble(uptakeMotor.current)
                disableUptakeEntry.setBoolean(disableUptake)
                shooterOneCurrentEntry.setDouble(shooterMotorOne.current)
                shooterTwoCurrentEntry.setDouble(shooterMotorTwo.current)
                rpmEntry.setDouble(rpm)
                rpmSetpointEntry.setDouble(rpmSetpoint)
            }
        }
    }

    override suspend fun default() {
        println("Shooter: Starting default")
        val t = Timer()
        var waitTime = 0.0
        var waiting = false
        t.start()
        periodic(period = 0.005) {
            if (!disableUptake) {
//                if (reverseBall) {
////                    println("REVERSING")
//                    uptakeMotor.setPercentOutput(-0.1)
//                    if (ballReady) {
//                        uptakeMotor.setPercentOutput(0.0)
//                        reverseBall = false
//                        println("exiting reverse")
//                        detectedBall = true
//                    } else {
//                        println("hiiiiiiiiiiii")
//                    }
//                    println("ball past sensor")
//                } else if (t.get() - waitTime > 0.1 && waiting) {
//                    println("finished time")
//                    reverseBall = true
//                    waiting = false
//                } else if (ballReady && !detectedBall) {
//                    println("detected a ball!!")
//                    waitTime = t.get()
//                    detectedBall = true
//                    waiting = true
//                }  else if (!ballReady) {
//                    uptakeMotor.setPercentOutput(1.0)
//                    Intake.detectedBall = false
//                }
                if (ballReady) {
                    uptakeMotor.setPercentOutput(0.0)
                } else {
                    uptakeMotor.setPercentOutput(1.0)
                }

//                println(shooterIdlePower)



                if (ballReady) {
                    shooterMotorOne.setPercentOutput(shooterIdlePower)
                    shooterMotorTwo.setPercentOutput(shooterIdlePower)
                    waitTime = t.get()
                } else if (t.get() - waitTime > 2.0) {
                    shooterMotorOne.setPercentOutput(0.1)
                    shooterMotorTwo.setPercentOutput(0.1)
                }
            } else {
                uptakeMotor.setPercentOutput(0.0)
                shooterMotorOne.setPercentOutput(0.0)
                shooterMotorTwo.setPercentOutput(0.0)
            }

        }
    }
}