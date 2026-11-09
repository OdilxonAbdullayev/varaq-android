# VARAQ 1.1 — Tekshiruv holati (2026-11-08)

## Bajarilgan tekshiruvlar

- [x] Manba koddagi noto‘g‘ri `rememberSaveable` importlari tuzatildi (5 ta Kotlin fayl).
- [x] Kitob holati tugatilganida sessiyani noto‘g‘ri qayta boshlamaslik ta'minlandi.
- [x] `Rejada` kitobning sahifa progressi bilan kelishuvchanligi tuzatildi.
- [x] Founder Reader import holatiga mos yangilandi.
- [x] AndroidManifest va resurs XML fayllari XML parser bilan tekshirildi.
- [x] Browser demo `node tests/browser_smoke.cjs` sinovidan o‘tdi (onboarding, kitob, o‘qish, statistika, qayd, sozlama).
- [x] PWA HTML va offline service worker JS sintaksisi `node --check` orqali tekshirildi.
- [x] Android APK avtomatik build uchun GitHub Actions workflow qo‘shildi.
- [x] GitHub Pages orqali PWA chiqarish workflow'i va ikonkalari qo‘shildi.

## Hali tekshirilmaganlar

- [ ] Android Gradle `assembleDebug` — Android SDK va internet mavjud emas.
- [ ] Kotlin / Compose haqiqiy compiler diagnostikasi.
- [ ] Android qurilma / emulyator testlari, bildirishnomalar ruxsati, muqova tanlash, Room ma'lumot saqlashi.
- [ ] PWA service worker'ning to‘liq browser offline testi — runtime browser bu muhitda administrator tomonidan bloklangan.
- [ ] GitHub Actions yuklanishi va GitHub Pages deployment — repository access berilmagan.
- [ ] Google Play nashr — developer akkaunti va 25 USD ro‘yxatdan o‘tish to‘lovi talab qilinadi.

**Xulosa:** ikkita mustaqil chiqarish yo‘li (native APK CI, PWA) qo‘shilgan source mavjud. *Hech bir APK tayyor yoki sinovdan o‘tgan deb da'vo qilinmaydi.*
