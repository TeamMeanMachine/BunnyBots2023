package org.team2471.bunnybots2023

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import edu.wpi.first.networktables.NetworkTableInstance
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.bunnybots2023.Limelight.toFieldCentric
import org.team2471.frc.lib.actuators.FalconID
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.radians
import kotlin.math.atan2

object Turret : Subsystem("Turret") {

    private val table = NetworkTableInstance.getDefault().getTable("Turret")
    val turretCurrentEntry = table.getEntry("Turret Current")
    val turretAngleEntry = table.getEntry("Turret Angle")
    val turretSetpointEntry = table.getEntry("Turret Setpoint")

// Same direction
    val turningMotor = MotorController(FalconID(Falcons.TURRET_TWO))

    val turretGearRatio: Double = 60.0/1.0

    // robot centric
    val deadzoneAngle : Angle = -130.0.degrees
    val deadzoneWidth : Angle = 10.0.degrees

    // in robot centric
    val turretAngle: Angle
        get() = turningMotor.position.degrees

    // in field centric
    var turretSetpoint: Angle = 0.0.degrees
        set(value) {
            println("HI!!!")
            val upperDeadzone : Angle = (deadzoneAngle + deadzoneWidth/2.0).toFieldCentric()
            val lowerDeadzone : Angle = (deadzoneAngle - deadzoneWidth/2.0).toFieldCentric()
            // coerce angle out of deadzone
            var angle = value.unWrap(deadzoneAngle)
            if (angle < upperDeadzone && angle >= deadzoneAngle) {
                angle = upperDeadzone
            } else if (angle > lowerDeadzone && angle <= deadzoneAngle) {
                angle = lowerDeadzone
            }
            angle = angle.wrap()
//            turningMotor.setPositionSetpoint(angle.toRobotCentric().asDegrees)
            field = angle
        }

    init {
//        println("*******************************************************************************************************")
        turningMotor.restoreFactoryDefaults()
        turningMotor.config() {
            //                          ticks / gear ratio
            feedbackCoefficient = 360.0 / 2048.0 / turretGearRatio

            brakeMode()
            inverted(false)
            pid {
                p(0.00002)
                d(0.00005)
            }
            currentLimit(0, 20, 0)

            encoderType(FeedbackDevice.IntegratedSensor)
            burnSettings()
            setRawOffsetConfig(0.0)
        }

        GlobalScope.launch(MeanlibDispatcher) {
            periodic {
                turretAngleEntry.setDouble(turretAngle.asDegrees)
                turretCurrentEntry.setDouble(turningMotor.current)
                turretSetpointEntry.setDouble(turretSetpoint.asDegrees)



            }
        }
    }

    override suspend fun default() {

        periodic {
            // sets joystickTarget to the current angle of the right joystick, null if at center
            val joystickTarget : Angle? = if (OI.driverController.rightThumbstick.length > Limelight.minJoystickDistance) {
                -atan2(OI.operatorRightY, OI.operatorRightX).radians
            } else {
                null
            }

            // handle joystick input
            if (joystickTarget != null) {

                val upperAimingBound : Angle = joystickTarget + 20.0.degrees
                val lowerAimingBound : Angle = joystickTarget - 20.0.degrees

                val target : BucketTarget? = Limelight.getBucketInBounds(upperAimingBound, lowerAimingBound)

                if (target != null) {
                    //aimAtBucket(target)
                } else {
                   // turretSetpoint = joystickTarget
                }

            } else {
                if (Limelight.enemyBuckets.isNotEmpty()) {
                    aimAtBucket(Limelight.enemyBuckets[0])
                }
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

}