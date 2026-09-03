package patches.shared.manifest

import app.morphe.patcher.patch.resourcePatch
import java.util.logging.Logger

@Suppress("unused")
val activeManifestProviderPatch = resourcePatch(
    name = "Add FGS Provider Hook (Test)",
    description = "Injects a ContentProvider that triggers the keep-alive service on process start, regardless of Application class structure.",
    default = false,
) {
    execute {
        var applied = false
        document("AndroidManifest.xml").use { m ->
            val root = m.documentElement ?: return@use
            val app = root.applicationOrNull() ?: return@use

            val packageName = root.getAttribute("package")
            if (packageName.isNullOrEmpty()) return@use

            // FGS permissions (same as original patch — safe to duplicate,
            // Android ignores duplicate uses-permission entries)
            val p1 = m.createElement("uses-permission")
            p1.setAttributeNS("http://schemas.android.com/apk/res/android", "android:name", "android.permission.FOREGROUND_SERVICE")
            val p2 = m.createElement("uses-permission")
            p2.setAttributeNS("http://schemas.android.com/apk/res/android", "android:name", "android.permission.FOREGROUND_SERVICE_REMOTE_MESSAGING")
            root.appendChild(p1)
            root.appendChild(p2)

            // The service itself (reused from KeepAliveService)
            val srv = m.createElement("service")
            srv.setAttributeNS("http://schemas.android.com/apk/res/android", "android:name", "app.morphe.extension.shared.KeepAliveService")
            srv.setAttributeNS("http://schemas.android.com/apk/res/android", "android:exported", "false")
            srv.setAttributeNS("http://schemas.android.com/apk/res/android", "android:foregroundServiceType", "remoteMessaging")
            app.appendChild(srv)

            // The provider hook — this is what triggers init() universally
            val provider = m.createElement("provider")
            provider.setAttributeNS("http://schemas.android.com/apk/res/android", "android:name", "app.morphe.extension.shared.KeepAliveInitProvider")
            provider.setAttributeNS("http://schemas.android.com/apk/res/android", "android:authorities", "$packageName.morphe.keepalive.testinit")
            provider.setAttributeNS("http://schemas.android.com/apk/res/android", "android:exported", "false")
            app.appendChild(provider)

            applied = true
        }
        if (applied) Logger.getLogger(this::class.java.name).info("Provider-based FGS hook patched (test variant)!")
    }
}
