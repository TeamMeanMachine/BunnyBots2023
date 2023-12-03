package org.team2471.bunnybots2023

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import edu.wpi.first.networktables.NetworkTableInstance
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.bunnybots2023.Limelight.toFieldCentric
import org.team2471.bunnybots2023.Limelight.toRobotCentric
import org.team2471.frc.lib.actuators.FalconID
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.radians
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.atan2

object Turret : Subsystem("Turret") {

    private val table = NetworkTableInstance.getDefault().getTable("Turret")
    val turretErrorEntry = table.getEntry("Turret Error")
    val turretCurrentEntry = table.getEntry("Turret Current")
    val turretAngleEntry = table.getEntry("Turret Angle")
    val fieldTurretSetpointEntry = table.getEntry("Field Centric Turret Setpoint")
    val robotTurretSetpointEntry = table.getEntry("Robot Centric Turret Setpoint")

    // Same direction
    val turningMotor = MotorController(FalconID(Falcons.TURRET_ONE), FalconID(Falcons.TURRET_TWO))

//                                Chain          Gears         Planetary
    val turretGearRatio: Double = (70.0/12.0) * (64.0/32.0) * (4.0)

    // robot centric
    val maxAngle : Angle = 100.0.degrees
    val minAngle : Angle = (-230.0).degrees

    // in robot centric
    val turretAngle: Angle
        get() = turningMotor.position.degrees

    val turretError: Angle
        get() = turretSetpoint.toRobotCentric() - turretAngle

    // in field centric
    var turretSetpoint: Angle = 0.0.degrees
        set(value) {
//            println("HI!!!")
            var angle = value.toRobotCentric()

            val minDist = (angle - minAngle).wrap()
            val maxDist = (angle - maxAngle).wrap()

            if (minDist.asDegrees.absoluteValue < maxDist.asDegrees.absoluteValue) {
                angle = angle.unWrap(minAngle)
            } else {
                angle = angle.unWrap(maxAngle)
            }

            angle = angle.asDegrees.coerceIn(minAngle.asDegrees, maxAngle.asDegrees).degrees
            turningMotor.setPositionSetpoint(angle.asDegrees)
            field = angle.toFieldCentric()
        }

    init {
//        println("*******************************************************************************************************")
        turningMotor.restoreFactoryDefaults()
        turningMotor.config() {
            //                            ticks / gear ratio             fudge factor
            feedbackCoefficient = (360.0 / 2048.0 / turretGearRatio) * (90.0/136.0)

            coastMode()
            inverted(false)
            pid {
                p(0.0000002)
                d(0.00005)
            }
            currentLimit(30, 40, 20)

            encoderType(FeedbackDevice.IntegratedSensor)
            burnSettings()
            setRawOffsetConfig(0.0)
        }

        GlobalScope.launch(MeanlibDispatcher) {
            periodic {
                turretAngleEntry.setDouble(turretAngle.asDegrees)
                turretCurrentEntry.setDouble(turningMotor.current)
                fieldTurretSetpointEntry.setDouble(turretSetpoint.asDegrees)
                robotTurretSetpointEntry.setDouble(turretSetpoint.toRobotCentric().asDegrees)
                turretErrorEntry.setDouble(turretError.asDegrees)
            }
        }
    }

    override suspend fun default() {

        periodic {
            // sets joystickTarget to the current angle of the right joystick, null if at center
            val joystickTarget : Angle? = if (OI.operatorController.rightThumbstick.length > Limelight.minJoystickDistance) {
                90.degrees + atan2(OI.operatorRightY, OI.operatorRightX).radians
            } else {
                null
            }

            // handle joystick input
            if (joystickTarget != null) {

                val upperAimingBound : Angle = joystickTarget + 20.0.degrees
                val lowerAimingBound : Angle = joystickTarget - 20.0.degrees

                val target : BucketTarget? = Limelight.getBucketInBounds(upperAimingBound, lowerAimingBound)

                if (target != null) {
                    aimAtBucket(target)
                } else {
                    turretSetpoint = joystickTarget
                }

            } else if (Limelight.enemyBuckets.isNotEmpty()) {
                aimAtBucket(Limelight.enemyBuckets[0])
            } else {
                turretSetpoint = turretSetpoint
                println("setpoint = $turretSetpoint")
            }



//            if (opX) {
//                println("whhhhhhhhyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy")
//                turretGO()
//            } else {
//                turretStop()
//            }
        }
    }

    fun aimAtBucket(target : BucketTarget){
        turretSetpoint = Limelight.getAngleToBucket(target)
    }

    fun turretRight() {
//        turningMotor.setPercentOutput(0.5)
//        delay(0.5)
        turretSetpoint = 90.0.degrees
    }


    fun turretLeft() {
//        turningMotor.setPercentOutput(0.0)
        turretSetpoint = (-90.0).degrees
    }

}