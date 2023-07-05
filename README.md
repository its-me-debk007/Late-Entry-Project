<p align="center">
  <img src="https://github.com/its-me-debk007/Late-Entry-Project/assets/81604986/5030b88a-5129-44db-9e5f-ad366562bc53" height="128">
  <h1 align="center">&nbsp; Late Entry Project</h1>
</p>

- Barcode scanner Android application, based on **MVVM Architecture**
- An **offline friendly** app
- Handled edge cases related to multiple clicks, multiple toasts, asking user permission, etc.
- GitHub Actions & Firebase App distribution for CI/CD to reduce testing time by **20%**
- Implemented R8 to reduce apk size by **28.5%**

## <h2 align=center>Purpose & Functionality of App 📱</h2>

### Purpose
This app is created for an official purpose **currently being used in our College**, in order to easily keep a track on students coming late to the college. 
The app data is synced with a remote server, where data is stored permanently.

### Functionality
- The user must **authenticate** himself first in order to start scanning
- The app provides a quick barcode scanner, with an option to enable flash
- In case of damaged camera/barcode, the user can **manually** enter student number and register a late entry
- On scanning or manually entering data, student details pops up with **name**, **branch**, **admission year**, **image**, & **student no** (which are stored in app's database to provide quicker action)
- In offline scenario, all entries are stored in **app's database**, & can be synced with the server afterwards
- The Settings page include the option to sync locally stored student details & late entry records and other options related to the user

#### Basic Working :-

https://github.com/its-me-debk007/Late-Entry-Project/assets/81604986/6e2c2668-f76f-45ab-8189-0b34ebe0df37

#### Offline Functionality :-

https://github.com/its-me-debk007/Late-Entry-Project/assets/81604986/ec633376-d6c7-444d-b4c2-3b5f457cecdf

## <h2 align=center>Screenshots 📸</h2>

||||
|:---:|:---:|:---:|
| <img src= "https://github.com/its-me-debk007/Late-Entry-Project/assets/81604986/12702caa-f1e1-40f0-a2d4-8020925efcee" width="220"> | <img src= "https://github.com/its-me-debk007/Late-Entry-Project/assets/81604986/47681cc8-c231-4373-a269-26d6fe3a1078" width="220"> | <img src= "https://github.com/its-me-debk007/Late-Entry-Project/assets/81604986/6b0449b4-4cee-4dd1-9f14-070540ec55d0" width="220"> |
| Splash | Login | Student Details |
| <img src= "https://github.com/its-me-debk007/Late-Entry-Project/assets/81604986/306738cb-a415-444e-af4f-100927731519" width="220"> | <img src= "https://github.com/its-me-debk007/Late-Entry-Project/assets/81604986/d7482603-a238-4b0b-ac9d-6edf20781dbe" width="220"> | <img src= "https://github.com/its-me-debk007/Late-Entry-Project/assets/81604986/babeab83-a95b-4ee6-aa77-ef37b3020135" width="220"> |
| Settings | Venue Selection | Exit Dialog |

## <h2 align=center>Tech Stacks Used 👩🏻‍💻</h2>
- Kotlin + Coroutines
- ViewModel + LiveData + ROOM DB + Preferences Datastore (Android Jetpack)
- Retrofit
- R8
- GitHub Actions
- Firebase App Distribution

#### Note:-
This is however, a copy of the actual repo! The actual repo contains a little extra amount of confidential code that can't be shared in a public repo.
