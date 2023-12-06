package org.team2471.bunnybots2023

import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.util.Timer
import kotlin.math.absoluteValue


suspend fun fire() = use(Shooter) {
    println("FIRING!!!! IM SHOOTING BALL")
    Shooter.shooterMotor.setPercentOutput(1.0)
    val t = Timer()
    var waitingTime = 0.0
    var previousCurrent = 999.0
    val ballWasLoaded = Intake.detectedBall
    periodic {
        val current = Shooter.shooterMotor.current
        println(listOf(current, previousCurrent))
        if ((Shooter.shooterMotor.current.absoluteValue - previousCurrent.absoluteValue).absoluteValue < 1.0) {
            println("current difference less then 1")
        } else {
            waitingTime = t.get()
        }
        if (t.get() - waitingTime > 0.04) {
            println("difference less then 1 for 0.05 seconds")
            this.stop()
        }
        previousCurrent = Shooter.shooterMotor.current

    }
    Shooter.uptakeMotor.setPercentOutput(1.0)
    delay(0.1)
    Shooter.reverseBall = false
    Intake.ballPast = ballWasLoaded
}
suspend fun toggleBallCollection() = use(Shooter, Intake) {
    if (Shooter.disableUptake) {
        Shooter.disableUptake = false
        Intake.disableConveyor = false
    } else {
        Shooter.disableUptake = true
        Intake.disableConveyor = true
    }
}
suspend fun holdToSpit() = use(Intake, Shooter) {
    val prevIntaking = Intake.intaking
    println("starting periodic")
    periodic {
        if (!OI.driverController.leftBumper) {
            this.stop()
        }
        Intake.centerMotor.setPercentOutput(-1.0)
        Intake.frontMotor.setPercentOutput(-1.0)
        Intake.conveyorMotor.setPercentOutput(-1.0)
        Shooter.uptakeMotor.setPercentOutput(-1.0)
    }
    println("stopping periodic")
    if (prevIntaking) {
        Intake.startIntake()
    } else {
        Intake.stopIntake()
    }
}
