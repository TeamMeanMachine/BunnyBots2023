package org.team2471.bunnybots2023

import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use


suspend fun fire() = use(Shooter) {
    println("firing")
    Shooter.uptakeMotor.setPercentOutput(1.0)
    Shooter.shooterMotorTwo.setPercentOutput(0.4)
    Shooter.shooterMotorOne.setPercentOutput(0.4)
    delay(0.1)
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
    periodic {
        if (OI.driverController.leftBumper) {
            this.stop()
        }
        Intake.centerMotor.setPercentOutput(-1.0)
        Intake.frontMotor.setPercentOutput(-1.0)
        Intake.conveyorMotor.setPercentOutput(-1.0)
        Shooter.uptakeMotor.setPercentOutput(-1.0)
    }
    if (prevIntaking) {
        Intake.startIntake()
    } else {
        Intake.stopIntake()
    }
}
