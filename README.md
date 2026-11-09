# VARAQ 1.1 — Qog‘oz kitoblar uchun shaxsiy Android kundalik

**Til:** O‘zbekcha (lotin) • **Platforma:** Android 8.0+ (API 26+) • **Byudjet:** $0 (o‘z telefoningizda yoki APK sifatida sinash uchun) • **Offline-first**

> Bu ZIP — **Android Studio manba loyihasi**. Android APK fayli ushbu arxivda YO‘Q: yaratilgan muhitda Android SDK/Gradle kutubxonalari va emulyator mavjud emas, shuning uchun build va qurilmada sinov o‘tkazilmadi. Pastdagi amallar yordamida kompyuteringizda o‘zingiz APK yig‘ishingiz mumkin. Birinchi marta Gradle va Android SDK’ni yuklab olish uchun internet kerak.

## 1. Nimalar ishlaydi?

- Ism, kunlik sahifalar va yillik kitoblar maqsadi bilan tanishtiruv (onboarding)
- Kitob qo‘shish / tahrirlash / o‘chirish: muallif, janr, rasm, sahifalar, holat, reyting va taassurot
- Uch holat: **Rejada**, **O‘qilmoqda**, **Tugatilgan**
- Shaxsiy kutubxona, qidirish va filtrlash
- Kitobning qancha qismi o‘qilgani, progress bar
- O‘qish taymeri, pauza, oxirgi sahifani kiritish va sessiyani saqlash
- Iqtiboslar va fikrlar, sahifa raqami bilan; qaydni o‘chirish
- Bugun o‘qilgan sahifalar, 7 kunlik grafik, o‘qish vaqti, ketma-ket kunlar, yillik maqsad
- Muvaffaqiyat belgilar: ilk sahifa, 7 kun, 5 kitob, 1000 sahifa
- Cream/Forest Green rangli dizayn va tungi rejim
- Android bildirishnoma ruxsati asosida taxminiy kunlik eslatma (08:00/13:00/20:00)
- Kitoblar, muqovalar, qaydlar va sessiyalarni **JSON zaxira faylga** eksport/import
- Maxsus do‘st sovg‘asi: **Founder Reader** belgisi va tabrik (kod: `VARAQ-BIRINCHI`)

**1.0 ga kirmaganlar:** Google orqali login, server sinxronlash, ISBN, OCR (suratdan matn olish), onlayn kitob klubi, do‘st profillari va ommaviy hamjamiyat. Bular 2.0/3.0 rejada. Hozirgi loyihada internet ruxsati (`INTERNET`) so‘ralmaydi.

## 2. APK avtomatik yig‘ish — 2 variant

### A) Windows: bitta fayl orqali (Android Studio va SDK o‘rnatilgan bo‘lsa)

`BUILD_APK_WINDOWS.bat` ustiga ikki marta bosing. Skript bir marta rasmiy Gradle’ni yuklaydi, SHA-256 bilan tekshiradi, keyin `assembleDebug`ni ishga tushiradi va `app\build\outputs\apk\debug\` papkasini ochadi. Buning uchun Android SDK kompyuteringizda mavjud va internet aloqasi ishlashi kerak. Skript tashqi muhitdagi har bir xatoni avtomatik tuzata olmaydi.

### B) Bepul GitHub Actions (kompyuterga Android Studio shart emas)

Loyihaning **ichidagi barcha fayllarni**, jumladan yashirin `.github` papkasini, GitHub repository **ildiziga** joylang. Repository `public` bo‘lsa, odatiy GitHub-hosted CI ishlarini bepul ishlatish mumkin. GitHub sahifasida **Actions → VARAQ - Android APK → Run workflow** ni bosing. Job muvaffaqiyatli tugagach, ish sahifasidagi **Artifacts → VARAQ-Android-debug-APK** orqali APK’ni oling. Ish haqi/limitlar va hisob talablari GitHub shartlariga bog‘liq. Bu workflow bu muhitda end-to-end sinov qilinmagan; xato chiqsa log orqali tuzatilishi kerak. Hisobingizni GitHub’ga sizning ruxsatingizsiz ula olmayman.

## 3. Windows'da noldan ochish (Android Studio orqali)

1. [Android Studio](https://developer.android.com/studio) o‘rnating. Birinchi o‘rnatishda Android SDK va Android Emulator komponentlariga ruxsat bering.
2. ZIP faylni `C:\Projects\VARAQ-Android` kabi oddiy joyga chiqaring. **ZIP ichidagi `VARAQ-Android` papkasining o‘zi** loyiha ildizi.
3. **`SETUP_WINDOWS.bat` faylini ikki marta bosing.** U Gradle 9.4.1 rasmiy tarqatilishini HTTPS orqali yuklaydi, SHA-256 ni tekshiradi va loyihaga rasmiy Gradle Wrapper (`gradlew.bat`, `gradle-wrapper.jar`) o‘rnatadi. Bu bir martalik ish; taxminan 132 MB Gradle yuklanadi.
4. Android Studio'ni oching → **File → Open** → `VARAQ-Android` papkasini tanlang → **Trust Project**.
5. `Sync Project with Gradle Files` jarayoni tugashini kuting. Agar SDK kerak desa, **SDK Platform 37** va **Build Tools 36.0.0** ni o‘rnating. Android Studio'ning o‘zi zarur kutubxonalarni yuklaydi.
6. Telefoningizda **Developer options → USB debugging** ni yoqing, USB orqali ulang. Yoki Android Studio ichidan emulyator yarating.
7. Yuqoridan `app` modulini va qurilmangizni tanlab, **Run ▶** bosing.
8. APK kerak bo‘lsa: **Build → Build Bundle(s) / APK(s) → Build APK(s)** (menyu nomi Studio versiyasiga qarab biroz farq qiladi).
9. Yaralgan debug APK odatda `app\build\outputs\apk\debug\app-debug.apk` manzilida bo‘ladi. Uni Telegram orqali do‘stingizga yuborib sinab ko‘rishingiz mumkin.

Terminalni afzal ko‘rsangiz, bir martalik setupdan keyin loyiha papkasida:

```powershell
.\gradlew.bat assembleDebug
```

**Muhim:** bu tavsiyalar hali qurilmada sinovdan o‘tkazilmagan manba kod uchun. Build xatolari yuzaga kelsa, xatolik matnini yuboring va mos tuzatishni birgalikda amalga oshirish mumkin.

## 4. macOS / Linux

```bash
bash SETUP_LINUX_MAC.sh
./gradlew assembleDebug
```

Avval mos Android SDK va Java 17+ o‘rnatilgan bo‘lishi lozim. Agar `ANDROID_HOME` talab qilinsa, uni SDK katalogiga qarating yoki Android Studio SDK sozlamalarini to‘g‘ri belgilang.

## 5. Arxitektura

```text
app/src/main/java/uz/varaq/app/
├── MainActivity.kt             # Asosiy navigatsiya, backup-fayl dialoglari
├── data/
│   ├── Database.kt             # Room: Book, BookNote, ReadingSession, DAO
│   └── Preferences.kt          # DataStore: ism, maqsadlar, tema, eslatmalar
├── model/
│   └── VaraqViewModel.kt       # Kirish tekshiruvi, CRUD, sessiyalar, backup
├── ui/
│   ├── Theme.kt                # Light/dark ranglar, tipografiya
│   ├── Components.kt           # Umumiy UI elementlar
│   ├── HomeAndLibrary.kt       # Bosh sahifa, kutubxona, kitob tafsiloti
│   ├── BookEditor.kt           # Kitob qo‘shish/tahrirlash, muqova tanlash
│   ├── ReadingAndJournal.kt    # Timer, iqtiboslar va qaydlar
│   └── StatisticsAndSettings.kt# Statistika, onboarding, sozlamalar
└── util/
    ├── FilesAndBackup.kt       # Rasm, JSON export/import
    └── Reminder.kt             # WorkManager va Notification
```

**Dasturlash:** Kotlin (AGP 9.2 built-in Kotlin 2.2.10), Jetpack Compose BOM 2026.09.00, Room 2.8.5 (KSP 2.3.10), DataStore, WorkManager. **Gradle:** 9.4.1; **compile SDK:** 37. Google rasmiy hujjatlaridagi mos versiyalar asosida tanlangan, ammo dastur tuzilmasi mahalliy Android SDK'da sinovdan o‘tkazilishi kerak.

## 6. Ma'lumotlarni saqlash va ko‘chirish

Ma'lumotlar qurilmada `Room`/`DataStore`da saqlanadi. Dastur o‘chirilsa, telefondagi ma'lumotlar ham ketishi mumkin: **Sozlamalar → Ma'lumotlarni eksport qilish** orqali JSON zaxira saqlang.

**Zaxiradan tiklash** telefondagi mavjud kitoblar, qaydlar va sessiyalarni **almashtiradi**. Ilova buning oldidan tasdiq so‘raydi. JSON zaxira fayli shifrlanmagan; ichida shaxsiy qaydlaringiz bo‘lishi mumkin, shuning uchun uni boshqalarga tarqatmang.

Muqova rasmlari telefon xotirasidagi xususiy papkaga kichraytirib saqlanadi (JPEG); shu bois rasm tanlagan ilova yoki asl foto keyin o‘chsa ham muqova qoladi.

## 7. Do‘sting uchun maxsus belgi

**Sozlamalar → Do‘stingiz uchun sovg‘a → Maxsus kod:**

`VARAQ-BIRINCHI`

Kodni kiritgan foydalanuvchida Founder Reader tabrigi ochiladi. Bu **offline sovg‘a kodi, haqiqiy autentifikatsiya emas** — kod dastur ichida bo‘ladi va uni nusxalash mumkin. Ommaviy tarqatishda shaxsiy xabarni o‘zgartiring yoki olib tashlang.

## 8. Cheklov va nashr qilish

- **Google Play** rasmiy ishlab chiquvchi akkaunti 0$ emas (ro‘yxatdan o‘tish to‘lovi bor); ilovani APK sifatida bepul sinab va ulashib ko‘rishingiz mumkin, lekin o‘rnatish paytida Android tasdiq so‘rashi mumkin.
- **Release APK** uchun `Build → Generate Signed Bundle / APK` da o‘zingizning maxfiy keystore kalitingizni yarating. **Keystore fayli va parolini hech kimga yubormang.**
- Play’ga chiqishdan avval sinov, maxfiylik siyosati, accessibility, zaxira xavfsizligi, hukumatning tegishli talablariga rioya etish va haqiqiy qurilmalarda tekshirish zarur.
- WorkManager vaqtli vazifalari telefonning energiya tejash tizimiga bog‘liq; belgilangan soatni kafolatlamaydi.
- Reading Timer ekrani yopilsa, **saqlanmagan** sessiya yo‘qoladi.
- Jamoaviy foydalanish va internet sinxronlash mavjud emas.

## 9. Hujjatlar va test

- [`docs/SMOKE_TESTS.md`](docs/SMOKE_TESTS.md) — qo‘lda tekshiriladigan 16 ta holat.
- [`docs/RELEASE_PLAN.md`](docs/RELEASE_PLAN.md) — 2.0/3.0 funksiyalar va publik versiya talablari.

Rasmiy havolalar: [Android Studio](https://developer.android.com/studio), [Android Gradle Plugin 9.2](https://developer.android.com/build/releases/agp-9-2-0-release-notes), [Compose BOM](https://developer.android.com/develop/ui/compose/bom), [Room](https://developer.android.com/jetpack/androidx/releases/room), [KSP](https://kotlinlang.org/docs/ksp-overview.html).

## 10. Telefonga APK'siz bepul o‘rnatish (PWA)

`web/index.html`, `web/manifest.webmanifest`, `web/sw.js`, `web/icons` PWA uchun tayyorlangan. GitHub Pages orqali HTTPS manzilga chiqarilgach, Android Chrome'da **Install app** orqali o‘rnatish mumkin. Buning uchun Native Android SDK kerak emas. Eslatmalar ishlamaydi va ma'lumotlar browser localStorage'da qoladi. [Batafsil qo‘llanma](docs/PWA_SETUP.md).

## 11. Dastlab ko‘rish uchun brauzer demosi

`VARAQ-BROWSER-DEMO.html` faylini Windows’da Chrome orqali ochishingiz mumkin. Bu telefon shaklidagi **alohida, interaktiv brauzer demo**, Android APK emas. Kitob qo‘shish, o‘qish sessiyasi, qaydlar, statistika, ranglar va JSON backupni brauzerning lokal xotirasida sinab ko‘rish imkonini beradi.

Ko‘rgazmali kitoblar bilan sinash uchun demoda: **Sozlamalar → Zaxiradan tiklash** → `docs/DEMO_DATA.json` ni tanlang. Bu fayldagi kitoblar va sessiyalar **faqat namuna** va shaxsiy real o‘qish tarixini ifodalamaydi. Demoda kiritilgan ma'lumotlar brauzer profili/tarixi tozalansa yo‘qolishi mumkin; kerakli ma'lumotni avval JSON'ga eksport qiling.

## Xolati va sinov haqida ochiq ma'lumot

- 2026-11-08: Kotlin `rememberSaveable` importlari tuzatildi, `Founder Reader` flag importida yangilash, kitob holati validatsiyasi va tugatilgan kitobning ishlamaydigan “qayta o‘qish” tugmasi tuzatildi.
- XML fayllari pars orqali tekshirildi; HTML interaktiv demo uchun Node.js smoke test bajarildi.
- Ushbu muhitda internet/DNS, Android SDK, Gradle dependency cache yo‘qligi sababli **Android APK build ham, emulyator/qurilma testi ham bajarilmadi**. GitHub Actions workflow to‘liq build urinishini boshqa muhitda amalga oshirish uchun berilgan, lekin ishga tushirilgani yo‘q.
- APK natijasi faqat build logi `success` bo‘lgandan keyin tayyor hisoblanadi.
