package org.team2471.bunnybots2023

import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.input.*
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.math.cube
import org.team2471.frc.lib.math.deadband
import org.team2471.frc.lib.math.squareWithSign
import org.team2471.frc.lib.motion.following.xPose

object OI : Subsystem("OI") {
    val driverController = XboxController(0)
    val operatorController = XboxController(1)

    private val deadBandDriver = 0.1
    private val deadBandOperator = 0.1

    private val driveTranslationX: Double
        get() = driverController.leftThumbstickX.deadband(deadBandDriver).squareWithSign()

    private val driveTranslationY: Double
        get() = driverController.leftThumbstickY.deadband(deadBandDriver).squareWithSign()

    val driveTranslation: Vector2
        get() = Vector2(driveTranslationX, -driveTranslationY) //does owen want this cubed?

    val driveRotation: Double
        get() = -(driverController.rightThumbstickX.deadband(deadBandDriver)).cube() // * 0.6

    val driveLeftTrigger: Double
        get() = driverController.leftTrigger

    val driveRightTrigger: Double
        get() = driverController.rightTrigger

    val operatorLeftTrigger: Double
        get() = operatorController.leftTrigger

    val operatorLeftY: Double
        get() = operatorController.leftThumbstickY.deadband(0.2)

    val operatorLeftX: Double
        get() = operatorController.leftThumbstickX.deadband(0.2)

    val operatorRightTrigger: Double
        get() = operatorController.rightTrigger

    val operatorRightX: Double
        get() = operatorController.rightThumbstickX.deadband(0.0)

    val operatorRightY: Double
        get() = operatorController.rightThumbstickY.deadband(0.0)

    init {
        driverController::back.whenTrue {
            Drive.zeroGyro()
            Drive.initializeSteeringMotors()
        }
        driverController::x.whenTrue { Drive.xPose() }
        driverController::rightBumper.whenTrue { Intake.toggleIntake() }
        driverController::b.whenTrue { toggleBallCollection() }
        driverController::leftBumper.whenTrue { holdToSpit() }



        operatorController::rightTriggerFullPress.whenTrue { fire() }
        operatorController::y.whenTrue { Intake.intakeUp()}
        operatorController::a.whenTrue { Intake.intakeDown()}
        operatorController::b.whenTrue { toggleBallCollection() }

    }
}
