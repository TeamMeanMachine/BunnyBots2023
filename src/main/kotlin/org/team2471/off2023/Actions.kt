package org.team2471.bunnybots2023

import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.framework.use
import org.team2471.off2023.Shooter


suspend fun fire() = use(Shooter) {
    Shooter.uptakeMotor.setPercentOutput(1.0)
    delay(0.1)
}
