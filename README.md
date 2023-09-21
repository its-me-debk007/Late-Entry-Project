![Language](https://img.shields.io/github/languages/top/its-me-debk007/Late-Entry-Project?color=B125EA&logo=kotlin&style=social)
&nbsp;
[![Owner](https://img.shields.io/badge/by-its--me--debk007-brightgreen?logo=github&style=social)](https://github.com/its-me-debk007)
&nbsp;
![License](https://img.shields.io/github/license/its-me-debk007/Late-Entry-Project?style=social)

<div align=center>
  
<img src="https://github.com/its-me-debk007/Late-Entry-Project/assets/81604986/d385aa4e-c615-4afa-8bd2-2e247cff7e91" height="128">

<h1 align=center>Late Entry Project</h1>

</div>

- Barcode scanner Android application, based on **MVVM Architecture**
- An **offline friendly** app
- Handled edge cases related to multiple clicks, multiple toasts, asking user permission, etc.
- GitHub Actions & Firebase App distribution for CI/CD to reduce testing time by **20%**
- Implemented R8 to reduce apk size by **28.5%**

## Purpose 📌
This app is created for an official purpose **currently being used in our College**, in order to easily keep a track on students coming late to the college. 
The app data is synced with a remote server, where data is stored permanently.

## Functionality 💻
- The user must **authenticate** himself first in order to start scanning
- The app provides a quick barcode scanner, with an option to enable flash
- In case of damaged camera/barcode, the user can **manually** enter student number and register a late entry
- On scanning or manually entering data, student details pops up with **name**, **branch**, **admission year**, **image**, & **student no** (which are stored in app's database to provide quicker access)
- In offline scenario, all entries are stored in **app's database**, & can be synced with the server afterwards
- The Settings page include the option to sync locally stored student details & late entry records and other options related to the user

## Basic Working ⚒

https://github.com/its-me-debk007/Late-Entry-Project/assets/81604986/5bd6e80e-2e27-4901-ac71-9bae28e65adc

## Offline Functionality 👨🏻‍🏭

https://github.com/its-me-debk007/Late-Entry-Project/assets/81604986/77f0aab4-8956-4f96-88b7-42d0ada588ba

## Preview 👀

| Splash | Login | Student Details |
|:---:|:---:|:---:|
| <img src= "https://github.com/its-me-debk007/Late-Entry-Project/assets/81604986/d66476a0-0752-4d87-aa78-481d664b2a43" width="220"> | <img src= "https://github.com/its-me-debk007/Late-Entry-Project/assets/81604986/34a53440-d6c5-4477-a548-69172d89db79" width="220"> | <img src= "https://github.com/its-me-debk007/Late-Entry-Project/assets/81604986/d050dcc3-6c61-4739-ad5c-e4a8a12771a5" width="220"> |

| Settings | Venue Selection | Exit Dialog |
|:---:|:---:|:---:|
| <img src= "https://github.com/its-me-debk007/Late-Entry-Project/assets/81604986/4d0669ba-9c57-4893-8fbb-02a749227dab" width="220"> | <img src= "https://github.com/its-me-debk007/Late-Entry-Project/assets/81604986/8abab614-5e0d-4079-be66-a48e43c92701" width="220"> | <img src= "https://github.com/its-me-debk007/Late-Entry-Project/assets/81604986/3a27b087-eb02-4be3-9eae-3f1b18b5439e" width="220"> |

## Tech Stack 👩🏻‍💻
- Kotlin + Coroutines
- ViewModel + LiveData + ROOM DB + Preferences Datastore (Android Jetpack)
- Retrofit
- R8
- GitHub Actions
- Firebase App Distribution
