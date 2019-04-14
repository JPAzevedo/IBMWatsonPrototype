# BRA - B# Recruitment App 

This app was developed during a recruiting process.<br/> 
The app calls some web services that can be found in the following url:

```http://docs.blissrecruitmentapi.apiary.io```

The user can find a list of questions about programming languages. The users are able to:
- Search and filter questions
- Get details about the questions
- Choose their option through a voting screen
- Share information by e-mail

## APIs

- [Gson][1]
- [OkHttp][2]
- [Picasso][3]

## Setup Instructions

1. Install the [Java Development Kit][4]

1. Install & setup the Android Studio

1. Download the code
```git clone https://github.com/JPAzevedo/IBMWatsonPrototype.git```

1. Import the project:
    1. Open the IDE (Android Studio) and choose "Import Project."
    1. Select the build.gradle file from the app folder.
		
1. Ensure you have downloaded the correct SDK (this app uses the API 28). If you don't have it, please download it.
   
1. Check dependencies such as Android Support Repository, Android Support Library, Google Play Services and Google Repository.

1. Clean and rebuild the project

1. Deploy the Android App
    1. Deploy your app via normal Android deployment procedures.

NOTE: This is a test. So there isn't any app key to official deploy the app on Google Play Store.
		
## Build Information 

- Java 7
- Gradle: 3.3.2
- minSdkVersion: 19
- targetSdkVersion: 28

[1]: https://github.com/watson-developer-cloud/android-sdk

