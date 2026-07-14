# Tolu's CC Android companion

This APK wraps the public command centre in a native WebView and provides a Samsung-compatible home-screen widget.

- The widget holds up to 24 active missions and uses `ViewFlipper` for a one-minute animated rotation.
- Priority controls the neon card colour: critical red, high amber, medium purple and low cyan.
- Completing a task removes it from the widget and stores a completion event locally.
- The next app launch presents each widget completion as an in-app debrief, then opens the batch source-update queue.
- Android controls final widget placement through its standard pin confirmation.

Build with `bash android-widget/build.sh`. The signed APK is written to `android-widget/build/Tolus-CC.apk`.
