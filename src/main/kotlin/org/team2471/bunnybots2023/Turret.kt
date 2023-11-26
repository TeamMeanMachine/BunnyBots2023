package org.team2471.bunnybots2023

import edu.wpi.first.networktables.NetworkTableInstance
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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


    val turningMotor = MotorController(FalconID(Falcons.TURRET_ONE), FalconID(Falcons.TURRET_TWO))

    val turretGearRatio: Double = 50.0/11.0

    val turretAngle: Angle
        get() = turningMotor.position.degrees

    var turretSetpoint: Angle = 0.0.degrees
        set(value) {
            val angle = value.asDegrees.coerceIn(-178.0, 178.0).degrees
            turningMotor.setPositionSetpoint(angle.asDegrees)
            field = angle
        }

    var spinning = false

    init {
        turningMotor.restoreFactoryDefaults()
        turningMotor.config(20) {
            //                          ticks / gear ratio
            feedbackCoefficient = 360.0 / 2048.0 / turretGearRatio

            brakeMode()
            inverted(false)
            pid {
                p(0.00002)
                d(0.00005)
            }
            currentLimit(0, 20, 0)
//            burnSettings()
        }
        turningMotor.setRawOffset(0.0)

        GlobalScope.launch(MeanlibDispatcher) {
            periodic {
                turretAngleEntry.setDouble(turretAngle.asDegrees)
                turretCurrentEntry.setDouble(turningMotor.current)

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
                        aimAtBucket(target)
                    } else {
                        turretSetpoint = joystickTarget
                    }

                } else {
                    if (Limelight.enemyBuckets.isNotEmpty()) {
                        aimAtBucket(Limelight.enemyBuckets[0])
                    }
                }

            }
        }
    }

    fun aimAtBucket(target : BucketTarget){
        turretSetpoint = Limelight.getAngleToBucket(target)
    }

}