# SPPU Engineering Question Papers App

[![Platform: Android](https://img.shields.io/badge/Platform-Android-green.svg)](#)
[![Scale: 130K+ Downloads](https://img.shields.io/badge/Scale-130K%2B%20Downloads-blue.svg)](#)

A production-grade, offline-first Android application designed to provide engineering students with seamless access to university question papers, syllabi, and academic timetables. Built to operate efficiently on low-end hardware and under volatile network conditions, the application successfully scaled to over **130,000+ organic users** with a highly rated user experience.

> **Archive Notice:** This repository serves as a verified architectural showcase and production archive. Due to legacy automated Google Play Store policy updates, the app is no longer actively listed on the store, but the verified scale analytics and production compiled assets are preserved below.

---

## 📊 Verified Scale & User Validation

During its active deployment window on the Google Play Store, the application achieved massive distribution and critical acclaim within the student community. 

### User Acquisition & Traction
Prior to archiving, the application crossed **130,000+ lifetime downloads** with high active retention during peak university examination windows.

<p align="center">
  <!-- Replace with the relative path to your downloads screenshot in the repository -->
  <img src="https://github.com/user-attachments/assets/b9660508-b2b6-4c8e-bb21-555119f51f93" alt="Google Play Console Analytics Showing 130K+ Downloads" width="1479" height="936" />
  <br>
  <i>Verified Google Play Console analytics dashboard tracking user acquisition scale.</i>
</p>

### Community Impact & Reviews
The application maintained an exceptional user rating, praised heavily for its lightning-fast offline access and UI utility when students needed it most.


<p align="center">
  <!-- Replace with the relative path to your reviews screenshot in the repository -->
  <img src="https://github.com/user-attachments/assets/01eba692-39b2-4db6-8853-1a4dd0e4fb31" alt="Play Store User Reviews" width="1468" height="767"/>
  <br>
  <i>Selected Play Store reviews highlighting real-world utility and real-time performance.</i>
</p>

---

## 🛠️ Technical Architecture & Highlights

Building an app used by over 130,000 students meant designing for high-concurrency data reads during exam seasons and minimizing client-side resource footprints.

* **Strategic Offline-First Architecture:** Recognized that university syllabi, exam patterns, and timetables operate on a predictable, cyclical 6-month academic calendar. Capitalizing on this low data-mutation rate, the app was architected to be entirely offline-first—eliminating redundant network requests, saving user bandwidth, and ensuring 100% uptime during high-stress exam periods.
* **Synchronization & Local Caching Tier:** Implemented a robust local SQLite caching layer paired with Firebase synchronization pipelines. Data is fetched once per semester and stored locally, guaranteeing zero internet dependency for core operations after the initial sync.
* **Highly Optimized Relational Schema:** Engineered a lightweight database schema to drastically reduce local storage overhead, guaranteeing fast query execution and smooth performance even on budget, low-end Android devices.
* **High-Concurrency Resilience:** Structured data delivery pipelines to gracefully handle massive organic traffic spikes right before university exam hours without server-side latency degradations.
---

## 🎛️ Tech Stack

* **Frontend/Core:** Java, Android SDK
* **Database & Caching:** SQLite (Local Architecture)
* **Backend Cloud Infrastructure:** Firebase (Realtime Database, Storage, Data Sync)

---

## 📦 Running & Testing the Production Build

For recruiters, engineering teams, or legacy users looking to run the live application asset:

1. Navigate to the **[Releases](../../releases)** tab of this repository.
2. Download the production-compiled binary: `SPPU-Question-Papers-v3.3.apk`.
3. Enable "Install Unknown Apps" (Sideloading) within your Android device's developer or security settings.
4. Transfer, install, and launch the application.
