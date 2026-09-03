package patches.shared.manifest

import app.morphe.patcher.patch.resourcePatch
import java.util.logging.Logger

@Suppress("unused")
val activeManifestPatch = resourcePatch(
    name = "Add FGS Permissions",
    description = "Injects Android 16 FGS Manifest rules",
    default = true,
) {
    execute {
        var applied = false
        document("AndroidManifest.xml").use { m ->
            val root = m.documentElement ?: return@use
            
            // Standard DOM XML parsing
            val appNodes = m.getElementsByTagName("application")
            if (appNodes.length == 0) return@use
            val app = appNodes.item(0)

            val p1 = m.createElement("uses-permission")
            p1.setAttributeNS("http://schemas.android.com/apk/res/android", "android:name", "android.permission.FOREGROUND_SERVICE")
            val p2 = m.createElement("uses-permission")
            p2.setAttributeNS("http://schemas.android.com/apk/res/android", "android:name", "android.permission.FOREGROUND_SERVICE_REMOTE_MESSAGING")
            root.appendChild(p1)
            root.appendChild(p2)

            val srv = m.createElement("service")
            // Naya Path yahan update kar diya gaya hai
            srv.setAttributeNS("http://schemas.android.com/apk/res/android", "android:name", "app.morphe.extension.all.versioncode.KeepAliveService")
            srv.setAttributeNS("http://schemas.android.com/apk/res/android", "android:exported", "false")
            srv.setAttributeNS("http://schemas.android.com/apk/res/android", "android:foregroundServiceType", "remoteMessaging")
            app.appendChild(srv)

            applied = true
        }
        if (applied) Logger.getLogger(this::class.java.name).info("Manifest FGS Patched for API 36!")
    }
}
