package org.team2471.off2023

object Shooter {
    val shooterMotor = null
    val shooterFollowerMotor = null

    val shooterSensor = null

    val rpm: Int
        get() = 0
    var rpmSetpoint: Int = 0
        set(value) { field = value }
}