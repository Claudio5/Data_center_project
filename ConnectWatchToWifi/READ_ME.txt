Instructions to connect your watch to ‘epfl’ wifi

    1. Connect your watch to your laptop
    2. Open a terminal (cmd) 
    3. Go to Android SDK folder and → platform-tools
    4. Type: adb install Wifi_v1.0.apk
    5. Start the app on the watch using the following adb commands
        ◦ adb shell am start -n de.stefant.connecttoradiuswifi/.MainActivity
    6. Insert following fields
        ◦ Network SSID: epfl
        ◦ Identity : your_epfl_username
        ◦ Password: your_epfl_password
        ◦ Eap-Method: peap
        ◦ Phase2-Method: none
    7. Click 'Connect'
