<img src="https://www.gnu.org/graphics/gplv3-88x31.png" alt="GPL v3 Logo">

# Warden : App management utility

*Warden* is a FOSS app management utility.

# Features

1. Free/Libre software - Has GPLv3 License
2. Beautiful design - Based on latest material design guidelines
3. Detects Trackers & Loggers across whole device
4. Allows to disable all trackers/loggers
5. Advance profile based app de-bloater

# Screenshots

<img src="https://gitlab.com/AuroraOSS/AppWarden/raw/master/fastlane/metadata/android/en-US/images/phoneScreenshots/ss01.png" height="400"><img src="https://gitlab.com/AuroraOSS/AppWarden/raw/master/fastlane/metadata/android/en-US/images/phoneScreenshots/ss02.png" height="400">
<img src="https://gitlab.com/AuroraOSS/AppWarden/raw/master/fastlane/metadata/android/en-US/images/phoneScreenshots/ss03.png" height="400"><img src="https://gitlab.com/AuroraOSS/AppWarden/raw/master/fastlane/metadata/android/en-US/images/phoneScreenshots/ss04.png" height="400">
<img src="https://gitlab.com/AuroraOSS/AppWarden/raw/master/fastlane/metadata/android/en-US/images/phoneScreenshots/ss05.png" height="400"><img src="https://gitlab.com/AuroraOSS/AppWarden/raw/master/fastlane/metadata/android/en-US/images/phoneScreenshots/ss06.png" height="400">
<img src="https://gitlab.com/AuroraOSS/AppWarden/raw/master/fastlane/metadata/android/en-US/images/phoneScreenshots/ss07.png" height="400">


# How does it work?

Warden has a static curated list of known trackers ([Exodus Privacy](https://reports.exodus-privacy.eu.org/en/)) , each app's dex file is read to retrieve the class names, 
these class names are then matched with the signatures of known trackers & loggers to find them.

A list of currently known trackers & loggers can be found [here](https://gitlab.com/AuroraOSS/AppWarden/-/blob/master/app/src/main/assets/trackers.json) & [here](https://gitlab.com/AuroraOSS/AppWarden/-/blob/master/app/src/main/assets/loggers.json)

Loggers in the context of Warden means all utilities which are used to log user activity on an app or logcat in general. Not all loggers are evil.
But few logging tools like ACRA, xLog are very powerful tools that can send user data to devs without user's consent.
So do read the app's Privacy Policy, beforehand.

Warden uses `su pm` to manage the components.

## De-Bloater (Requires Root)
Warden provides a profile based de-bloater where a profile is created in a format specified as in this sample scripts
You need to place this profile/your custom profile at `ExternalStorage/Warden/Profiles` to make them appear in app.

De-Bloater is an experimental feature, will improve it over time.
Default action for debloating is 'disable' you can configure it to 'uninstall' or 'hide' from settings.

## Nuke it! (Requires Root)
Nuke it! is another experimental feature that scans all apps on the device and disables all know tracker components automatically.
It also gives an option to export components names per-app basis.

Components here means: Activities, Services, Providers & Receivers 


# Support Developement

<img src="https://img.shields.io/static/v1?label=Bitcoin&message=bc1qu7cy9fepjj309y4r2x3rymve7mw4ff39c8cpe0&color=Orange">

<img src="https://img.shields.io/static/v1?label=Bitcoin Cash&message=qpqus3qdlz8guf476vwz0fjl8s34fseukcmrl6eknl&color=Success">

<img src="https://img.shields.io/static/v1?label=Ethereum&message=0x6977446933EC8b5964D921f7377950992337B1C6&color=Blue">

<img src="https://img.shields.io/static/v1?label=BHIM UPI&message=whyorean@dbs&color=BlueViolet">

* Paypal - [Link](https://paypal.me/AuroraDev)
* LiberaPay - [Link](https://liberapay.com/on/gitlab/whyorean/)
  
# Links
* XDA Labs - [Link](https://labs.xda-developers.com/store/app/com.aurora.warden)
* Support Group - [Telegram](https://t.me/AuroraOfficial)

# Warden uses the following major Open Source libraries:

* [RX-Java](https://github.com/ReactiveX/RxJava)
* [ButterKnife](https://github.com/JakeWharton/butterknife)
* [Retrofit](https://square.github.io/retrofit/)
* [Fastadapter](https://github.com/mikepenz/FastAdapter)
* [Lombok](https://github.com/noties/Markwon)
* [Markwon](https://github.com/noties/Markwon)
* [LibSu](https://github.com/topjohnwu/libsu)
* [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
* [ExpansionPanel](https://github.com/florent37/ExpansionPanel)

