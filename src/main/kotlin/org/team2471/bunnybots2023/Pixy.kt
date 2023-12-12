package org.team2471.bunnybots2023

import io.github.pseudoresonance.pixy2api.Pixy2
import io.github.pseudoresonance.pixy2api.Pixy2CCC
import io.github.pseudoresonance.pixy2api.links.SPILink

import edu.wpi.first.networktables.NetworkTableInstance
import org.team2471.frc.lib.framework.Subsystem
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.periodic

object Pixy : Subsystem("Pixy") {

    val table = NetworkTableInstance.getDefault().getTable("Pixy")

    lateinit var pixy : Pixy2

    init {

    }

    override fun preEnable() {
        initializePixy()
    }

    override suspend fun default() {
        periodic {
            if (pixy != null) {
//                println("pixy FPS: ${ pixy.fps }")
            }
        }
    }

    fun initializePixy() {
        pixy = Pixy2.createInstance(SPILink())
        pixy.init()
        pixy.setLamp(0.toByte(), 0.toByte())
    }
}



