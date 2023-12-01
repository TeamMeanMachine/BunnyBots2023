package org.team2471.bunnybots2023

import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use


suspend fun fire() = use(Shooter) {
    println("FIRING!!!! IM SHOOTING BALL")
    Shooter.uptakeMotor.setPercentOutput(1.0)
    Shooter.reverseBall = false
    delay(0.2)
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
