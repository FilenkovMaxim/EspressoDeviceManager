# EspressoDeviceManager
App for change system settings during Espresso test run.

Installation steps:
1. Create and run Genymotion emulator device.
2. Enable network (and GPS if you need it)
3. Click "Open GAPPS" button and install GooglePlay.
4. Run Play Store and set Google account.
5. Install SuperSu aps from Google Play
6. Download UPDATE-SuperSU-v2.46.zip from https://download.chainfire.eu/696/supersu and drag it to emulator.
7. Reboot.
8. Install Espresso Device Manager apk and set Yes for "Modify system settings" in App info.
9. Run Espresso Device Manager apk and grant root access.
10. Verify work by change switches on a main screen.
11. In the SuperSu settings set Default access to Grant option.
Ready to work!