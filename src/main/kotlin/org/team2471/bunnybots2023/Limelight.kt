package org.team2471.bunnybots2023

import edu.wpi.first.networktables.NetworkTableInstance
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.bunnybots2023.Limelight.bucketWidth
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.feet

object Limelight : Subsystem("Limelight") {
    private val datatable = NetworkTableInstance.getDefault().getTable("limelight-front")
    private val validTargetsEntry = datatable.getEntry("tv")
    private val ledModeEntry = datatable.getEntry("ledMode")


    private const val lengthHeightMinRatio = 2.5
    const val limelightHeight = 16 // inches
    const val limelightScreenWidth = 320
    const val limelightScreenHeight = 320
    const val bucketWidth = 10 + 2/8 // Inches

    var enemyBuckets : List<BucketTarget> = arrayListOf<BucketTarget>()

    var lastJoystickTarget: Angle = 0.0.degrees

    // field centric
    val limelightAngle : Angle
        get() = Turret.turretAngle + Drive.heading


    init {

        GlobalScope.launch(MeanlibDispatcher) {

            periodic {
                enemyBuckets = identifyBuckets()
                if (enemyBuckets.isNotEmpty()) {
//                    for (i in filteredTargets!!) {
//                        println("ID: ${i.id}, IsRed: ${i.isRed} IsValid: ${i.isRed == FieldManager.isBlueAlliance}")
//                    }
                    enemyBuckets = enemyBuckets.filter {
                        it.isRed == AutoChooser.redSide
                    }
                }

                validTargetsEntry.setBoolean(seesTargets)
            }
        }
    }

    // returns field-centric angle to bucket
    fun getAngleToBucket(bucket: BucketTarget) : Angle {
        return limelightAngle + Angle.atan(bucket.x * (29.8).degrees.tan())
    }

    fun Angle.toFieldCentric() : Angle {
        return this + Drive.heading
    }

    fun Angle.toRobotCentric() : Angle {
        return this - Drive.heading
    }

    val seesTargets: Boolean
        get() = enemyBuckets.isNotEmpty()

    // gets a bucket in field-centric bounds
    fun getBucketInBounds(upperBound: Angle, lowerBound: Angle) : BucketTarget? {
        var foundTarget : BucketTarget? = null
        for (target in enemyBuckets.indices) {
            val angleToBucket : Angle = getAngleToBucket(enemyBuckets[target]).unWrap((upperBound + lowerBound)/2.0)
            if (lowerBound < angleToBucket &&
                angleToBucket < upperBound) {
                foundTarget = enemyBuckets[target]
                break
            }
        }
        return foundTarget
    }

    fun identifyBuckets(): List<BucketTarget> {

        // find all long strips
        var longStrips = arrayListOf<Int>()
        for (entryNum in 0..7) {
            //println(datatable.getEntry("thor${entryNum}").getDouble(0.0) / datatable.getEntry("tvert${entryNum}").getDouble(0.0))
            if (datatable.getEntry("thor${entryNum}").getDouble(0.0) / datatable.getEntry("tvert${entryNum}").getDouble(0.0) >= lengthHeightMinRatio) {
                longStrips.add(entryNum)
            }
        }
//        println("LongStrips: ${longStrips}")
        // find color of long strips
        var longStripsColor = IntArray(longStrips.size) {0}
        for (entryNum in 0 .. 7) {
            if (longStrips.contains(entryNum)) continue
            if (datatable.getEntry("ta${entryNum}").getDouble(0.0) == 0.0) continue

            val shortStripX = datatable.getEntry("tx${entryNum}").getDouble(0.0)
            val shortStripY = datatable.getEntry("ty${entryNum}").getDouble(0.0)

            for (i in 0 until longStrips.size) {
                val target = longStrips[i]
                val longStripX = datatable.getEntry("tx${target}").getDouble(0.0)
                val longStripHorizontal = datatable.getEntry("thor${target}").getDouble(0.0) / (0.5 * limelightScreenWidth)
                val longStripY = datatable.getEntry("ty${target}").getDouble(0.0)
                if (shortStripX < longStripX + longStripHorizontal/2 &&
                    shortStripX > longStripX - longStripHorizontal/2) {
                    if (shortStripY > longStripY) {
                        longStripsColor[i] -= 1
                    } else {
                        longStripsColor[i] += 1
                    }
                }
//                println("X: $shortStripX $longStripX $longStripHorizontal")
            }
        }


        var targets = arrayListOf<BucketTarget>()
        for (i in 0 until longStrips.size) {
//            println("id: ${longStrips[i]}: " + longStripsColor[i])
            if (longStripsColor[i] == 0) continue // color for long strip is not known
            targets.add(BucketTarget(
                longStrips[i],
                longStripsColor[i] > 0,
                datatable.getEntry("tx${longStrips[i]}").getDouble(0.0),
                datatable.getEntry("ty${longStrips[i]}").getDouble(0.0),
                datatable.getEntry("thor${longStrips[i]}").getDouble(0.0)
            ))
        }
//
//        for (target in targets) {
//            println("Target ${target.id} angleWidth: ${target.angleWidth} ")
//        }

        return targets
    }
    fun targetNum(): Int {
        var amount: Int = 0
        for (entryNum in 0..7) {
            if (datatable.getEntry("ta${entryNum}").getDouble(0.0) != 0.0) {
                println("found tag ta${entryNum}")
                amount += 1
            }
        }
        return amount
    }

    fun toggleLight() {
        if (ledModeEntry.getDouble(0.0).toInt() == 1) {
            println("turning on limelight")
            ledModeEntry.setDouble(0.0)
        } else {
            println("turning off limelight")
            ledModeEntry.setDouble(1.0)
        }
    }

}
data class BucketTarget (
    val id: Int,
    val isRed: Boolean,
    val x: Double,
    val y: Double,
    val pixelWidth: Double
) {
    val angleWidth = Angle.atan(pixelWidth * (29.8).degrees.tan())
    val dist = ((bucketWidth / 2) / (angleWidth / 2.0).tan()).feet
}