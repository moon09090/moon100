AryanOverlay - draws the comet + "Yes Aryan" animation over your REAL home screen
(transparent, touch-through, lightly blurred). Termux triggers it.

BUILD (free, in the cloud):
 1. Create an empty GitHub repo (e.g. aryan-overlay).
 2. In Termux:
      pkg install git unzip
      unzip aryan_overlay.zip -d aryan_overlay && cd aryan_overlay
      git init && git add -A && git commit -m init
      git branch -M main
      git remote add origin https://github.com/YOUR_NAME/aryan-overlay.git
      git push -u origin main      (password = a GitHub personal access token)
 3. GitHub -> Actions tab -> wait for "Build APK" to finish (green).
 4. Open that run -> Artifacts -> AryanOverlay-apk -> download, unzip, install app-debug.apk.

FIRST RUN:
 - Open "Aryan Overlay", allow "Display over other apps" and notifications.
 - Tap "Test Yes Aryan overlay" to preview.
 - Phone settings: set the app's battery to "Unrestricted" so it is not killed.

TERMUX TRIGGER (boss_ai.py does this for you):
   am broadcast -a com.aryan.overlay.SHOW --es text "Yes Aryan" --ei ms 6000

Blur strength: BLUR_RADIUS in OverlayService.kt (needs Android 12+ and a phone that supports window blur).
