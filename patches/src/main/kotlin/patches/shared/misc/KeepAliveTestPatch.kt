package patches.shared.misc

import app.morphe.patcher.patch.resourcePatch
import app.morphe.patches.all.misc.extension.sharedExtensionPatch
import java.util.logging.Logger

@Suppress("unused")
val keepAliveTestPatch = resourcePatch(
    name = "Keep Alive (Provider Test)",
    description = "Standalone test: triggers a foreground keep-alive service via ContentProvider, independent of Application class.",
    default = false,
) {
    dependsOn(sharedExtensionPatch())

    execute {
        var applied = false
        document("AndroidManifest.xml").use { m ->
            val root = m.documentElement ?: return@use
            val appNodes = m.getElementsByTagName("application")
            if (appNodes.length == 0) return@use
            val app = appNodes.item(0) as org.w3c.dom.Element
            val packageName = root.getAttribute("package")
            if (packageName.isNullOrEmpty()) return@use

            val ns = "http://schemas.android.com/apk/res/android"

            val p1 = m.createElement("uses-permission")
            p1.setAttributeNS(ns, "android:name", "android.permission.FOREGROUND_SERVICE")
            val p2 = m.createElement("uses-permission")
            p2.setAttributeNS(ns, "android:name", "android.permission.FOREGROUND_SERVICE_REMOTE_MESSAGING")
            root.appendChild(p1)
            root.appendChild(p2)

            val srv = m.createElement("service")
            srv.setAttributeNS(ns, "android:name", "app.morphe.extension.shared.KeepAliveTestService")
            srv.setAttributeNS(ns, "android:exported", "false")
            srv.setAttributeNS(ns, "android:foregroundServiceType", "remoteMessaging")
            app.appendChild(srv)

            val provider = m.createElement("provider")
            provider.setAttributeNS(ns, "android:name", "app.morphe.extension.shared.KeepAliveTestInitProvider")
            provider.setAttributeNS(ns, "android:authorities", "$packageName.morphe.keepalivetest.init")
            provider.setAttributeNS(ns, "android:exported", "false")
            app.appendChild(provider)

            applied = true
        }
        if (applied) Logger.getLogger(this::class.java.name).info("Keep Alive (Provider Test) patched!")
    }
}
