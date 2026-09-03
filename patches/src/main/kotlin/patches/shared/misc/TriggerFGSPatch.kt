package patches.shared.misc

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import java.util.logging.Logger

// Perfect Import with app.morphe prefix
import app.morphe.patches.all.misc.extension.sharedExtensionPatch

@Suppress("unused")
val triggerFGSPatch = bytecodePatch(
    name = "Trigger Immortal FGS",
    description = "Starts a persistent foreground keep-alive service shortly after the app launches.",
    default = true,
) {
    // Fixed with Brackets ()
    dependsOn(sharedExtensionPatch())

    execute {
        val logger = Logger.getLogger(this::class.java.name)
        var hooked = false

        classDefForEach { c ->
            // Scan for standard Application classes
            val isAppClass = c.superclass == "Landroid/app/Application;" || 
                             c.superclass == "Landroidx/multidex/MultiDexApplication;" || 
                             c.type == "Lcom/x/android/XApplication;"

            if (isAppClass) {
                val mClass = mutableClassDefBy(c)
                val targetMethod = mClass.methods.find { it.name == "onCreate" && it.returnType == "V" && it.parameterTypes.isEmpty() }

                if (targetMethod != null && targetMethod.implementation != null) {
                    try {
                        // Naya Path yahan update kar diya gaya hai
                        val smali = "invoke-static {p0}, Lapp/morphe/extension/all/versioncode/KeepAliveService;->init(Landroid/app/Application;)V"
                        val insts = targetMethod.implementation!!.instructions
                        
                        val retIdx = insts.indexOfLast { it.opcode.name == "return-void" }
                        
                        if (retIdx != -1) {
                            targetMethod.addInstructions(retIdx, smali)
                            hooked = true
                            logger.info("Immortal FGS trigger safely hooked into ${mClass.type}->onCreate")
                        }
                    } catch (e: Exception) {
                        logger.warning("Injection failed on ${mClass.type}")
                    }
                }
            }
        }
        
        if (!hooked) {
            logger.warning("No Application.onCreate found. No changes applied.")
        }
    }
}
