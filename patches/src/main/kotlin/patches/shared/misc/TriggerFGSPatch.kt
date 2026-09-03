package patches.shared.misc

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
// Note: Agar naye repo me StartupHooks ka folder badla hai, toh in imports ko us hisaab se theek kar lijiyega.
import patches.universal.ads.util.cloneMutable
import patches.universal.ads.util.p0Register
import patches.universal.ui.StartupHooks
import patches.universal.ui.findApplicationOnCreate
import java.util.logging.Logger

@Suppress("unused")
val triggerFGSPatch = bytecodePatch(
    name = "Trigger Immortal FGS",
    description = "Starts a persistent foreground keep-alive service shortly after the app launches.",
    default = true,
) {
    dependsOn(StartupHooks.resolveRealApplicationPatch)

    execute {
        val logger = Logger.getLogger(this::class.java.name)

        val (mutableClass, onCreate) = run {
            val descriptor = StartupHooks.resolvedApplicationDescriptor
            if (descriptor != null) {
                val cls = mutableClassDefByOrNull(descriptor)
                val om = cls?.methods?.firstOrNull {
                    it.name == "onCreate" && it.returnType == "V" && it.parameterTypes.isEmpty()
                }
                if (cls != null && om != null) {
                    return@run cls to om
                }
            }
            findApplicationOnCreate()
        } ?: run {
            logger.warning("No Application.onCreate found. No changes applied.")
            return@execute
        }

        val cloned = onCreate.cloneMutable(additionalRegisters = 0)
        val contextReg = cloned.p0Register

        cloned.addInstructions(
            0,
            """
            invoke-static/range {v$contextReg .. v$contextReg}, Lapp/morphe/extension/shared/KeepAliveService;->init(Landroid/app/Application;)V
            """.trimIndent(),
        )

        mutableClass.methods.remove(onCreate)
        mutableClass.methods.add(cloned)

        logger.info("Immortal FGS trigger hooked into ${mutableClass.type}->onCreate")
    }
}

