package org.team2471.bunnybots2023

import edu.wpi.first.networktables.NetworkTableEvent
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.coroutines.delay
//import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.parallel
import org.team2471.frc.lib.coroutines.periodic
//import org.team2471.bunnybots2022.Drive
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.motion.following.drive
import org.team2471.frc.lib.motion.following.driveAlongPath
import org.team2471.frc.lib.motion_profiling.Autonomi
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.util.Timer
import org.team2471.frc.lib.util.measureTimeFPGA
import java.io.File
import java.util.*

private lateinit var autonomi: Autonomi


enum class Side {
    LEFT,
    RIGHT;

    operator fun not(): Side = when (this) {
        LEFT -> RIGHT
        RIGHT -> LEFT
    }
}

private var startingSide = Side.RIGHT
val selAuto
    get() = SmartDashboard.getString("Autos/selected", "no auto selected")

object AutoChooser {
    private val isRedAllianceEntry = NetworkTableInstance.getDefault().getTable("FMSInfo").getEntry("isRedAlliance")
    private var autonomiEntryTopicSub =
        NetworkTableInstance.getDefault().getTable("PathVisualizer").getStringTopic("Autonomi").subscribe("")

    var cacheFile: File? = null
    var redSide: Boolean = true
        get() = isRedAllianceEntry.getBoolean(true)
        set(value) {
            field = value
            isRedAllianceEntry.setBoolean(value)
        }

    private val lyricsChooser = SendableChooser<String?>().apply {
        setDefaultOption("Country roads", "Country roads")
        addOption("take me home", "take me home")
    }

    private val testAutoChooser = SendableChooser<String?>().apply {
        addOption("None", null)
        addOption("20 Foot Test", "20 Foot Test")
        addOption("8 Foot Straight", "8 Foot Straight")
        addOption("2 Foot Circle", "2 Foot Circle")
        addOption("4 Foot Circle", "4 Foot Circle")
        addOption("8 Foot Circle", "8 Foot Circle")
        addOption("Hook Path", "Hook Path")
        setDefaultOption("90 Degree Turn", "90 Degree Turn")


    }

    private val autonomousChooser = SendableChooser<String?>().apply {
        setDefaultOption("Tests", "testAuto")
        addOption("Outer Three Auto", "outerThreeAuto")
        addOption("Outer Two Auto", "outerTwoAuto")
        addOption("Inner Three Auto", "innerThreeAuto")
        addOption("NodeDeck", "nodeDeck")
        addOption("BunnyBot2023", "BunnyBot2023")

    }

    init {
//        DriverStation.reportWarning("Starting auto init warning", false)
//        DriverStation.reportError("Starting auto init error", false)         //            trying to get individual message in event log to get timestamp -- untested

        SmartDashboard.putData("Best Song Lyrics", lyricsChooser)
        SmartDashboard.putData("Tests", testAutoChooser)
        SmartDashboard.putData("Autos", autonomousChooser)

        try {
            cacheFile = File("/home/lvuser/autonomi.json")
            if (cacheFile != null) {
                autonomi = Autonomi.fromJsonString(cacheFile?.readText())!!
                println("Autonomi cache loaded.")
            } else {
                println("Autonomi failed to load!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! RESTART ROBOT!!!!!!")
            }
        } catch (_: Throwable) {
            DriverStation.reportError("Autonomi cache could not be found", false)
            autonomi = Autonomi()
        }
        println("In Auto Init. Before AddListener. Hi.")
        NetworkTableInstance.getDefault().addListener(
            autonomiEntryTopicSub,
            EnumSet.of(
                NetworkTableEvent.Kind.kImmediate,
                NetworkTableEvent.Kind.kPublish,
                NetworkTableEvent.Kind.kValueAll
            )
        ) { event ->
            println("Autonomous change detected")
            if (event.valueData != null) {
                val json = event.valueData.value.string
                if (json.isNotEmpty()) {
                    val t = measureTimeFPGA {
                        autonomi = Autonomi.fromJsonString(json) ?: Autonomi()
                    }
                    println("Loaded autonomi in $t seconds")
                    if (cacheFile != null) {
                        println("CacheFile != null. Hi.")
                        cacheFile!!.writeText(json)
                    } else {
                        println("cacheFile == null. Hi.")
                    }
                    println("New autonomi written to cache")
                } else {
                    autonomi = Autonomi()
                    DriverStation.reportWarning("Empty autonomi received from network tables", false)
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun bunnyBot2023() = use(Drive) {
        println("inside bunnyBot2023() auto function")
        Drive.initializeSteeringMotors()
        Drive.zeroGyro()
        val totePath = autonomi["BunnyBot2023"]?.get("MoveToTotes")
        if (totePath != null) {
            parallel({
                println("GONNA DRIVE NOWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW")
                Drive.driveAlongPath(totePath, true)
            }, {
                val t = Timer()
                var waitTime = 0.0
                t.start()
                periodic {
                    if (Limelight.seesTargets && t.get() - waitTime > 1.0 || !Robot.isAutonomous) {
                        println("saw target for more then 1 second, aborting path")
                        Drive.abortPath()
                        this.stop()
                    } else {
                        waitTime = t.get()
                    }
                }
            })
            val t = Timer()
            var seeTime = 0.0
            t.start()
            periodic {
                if (Robot.isAutonomous) {
                    if (Limelight.seesTargets) {
                        println("driving to bucket at ${Limelight.enemyBuckets[0].botCentCoords + Drive.position} from ${Drive.position}")

                        Drive.drive(
                            Vector2(
                                Limelight.enemyBuckets[0].botCentCoords.y/*Limelight.botCentFilterX.calculate(Limelight.enemyBuckets[0].botCentCoords.x)*/,
                                Limelight.enemyBuckets[0].botCentCoords.x/*Limelight.botCentFilterY.calculate(Limelight.enemyBuckets[0].botCentCoords.y)*/
                            ),
                            0.0,
                            false
                        )
                        if (t.get() - seeTime > 1.0) {
                            println("saw target for more then 1 second, shooting")
                            Shooter.uptakeMotor.setPercentOutput(1.0)// <- this may or may not work
                        } else if (t.get() - seeTime > 1.2) {
                            println("shot ball stopping uptake")
                            Shooter.uptakeMotor.setPercentOutput(0.0)
                            seeTime = t.get()
                        }
                        //   turret shoot at target
                    } else {
                        Drive.drive(
                            Vector2(0.0, 0.0),
                            0.0,
                            false
                        )
                        Shooter.uptakeMotor.setPercentOutput(0.0)
                        println("i no see")
                        Turret.rawTurretSetpoint += 5.0.degrees
                        seeTime = t.get()
                    }
                } else {
                    this.stop()
                }
            }
            //drive to target and shoot
//                }

        } else {
            println("BUNNYBOTS PATH IS NULL!!!!!!!!")
        }

        // drive, shoot, intake, intake motors, limeLight
    }

    suspend fun autonomous() = use(Drive, name = "Autonomous") {
        println("Got into Auto fun autonomous. Hi. 888888888888888 ${Robot.recentTimeTaken()}")
        SmartDashboard.putString("autoStatus", "init")
        println("Selected Auto = *****************   $selAuto ****************************  ${Robot.recentTimeTaken()}")
        when (selAuto) {
            "Tests" -> testAuto()
            "BunnyBot2023" -> bunnyBot2023()
            else -> println("No function found for ---->$selAuto<-----  ${Robot.recentTimeTaken()}")
        }
        SmartDashboard.putString("autoStatus", "complete")
        println("finished autonomous  ${Robot.recentTimeTaken()}")
    }


    private suspend fun testAuto() {
        val testPath = SmartDashboard.getString("Tests/selected", "no test selected") // testAutoChooser.selected
        if (testPath != null) {
            val testAutonomous = autonomi["Tests"]
            val path = testAutonomous?.get(testPath)
            if (path != null) {
                Drive.driveAlongPath(path, true)
            }
        }
    }




}


