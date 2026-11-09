# VARAQ — telefonga bepul veb ilova (PWA) o‘rnatish

## Bu nima?

`web/` papkasidagi VARAQ — brauzerdagi ishchi prototip asosida tayyorlangan **offline PWA**. Unda kitoblar, o‘qish vaqti, qaydlar, statistika va JSON eksport/import mavjud. Veb versiyada Android native bildirishnomalari yo‘q. **Bu APK emas.**

## Telefoningizga ilova kabi o‘rnatish

1. `VARAQ-Android` papkasining ichidagi hamma fayllarni (yashirin `.github` papkasi bilan) GitHub repository'ga yuklang. Public repository GitHub Pages uchun qulay.
2. Repository Settings → Pages → Build and deployment → Source: **GitHub Actions** qilib qo‘ying.
3. Actions → **VARAQ - Bepul veb ilova (PWA)** → Run workflow.
4. Deployment muvaffaqiyatli bo‘lgach, Settings → Pages manzilidagi HTTPS linkni Android Chrome'da oching.
5. Chrome menyusida **Install app / Ilovani o‘rnatish** (yoki Add to Home Screen) ni tanlang.
6. Internetni o‘chirib, ilovani sinab ko‘rishingiz mumkin. PWA birinchi yuklangan paytda resurslarni keshga oladi.

**Ogohlantirish:** Kitoblar brauzerning lokal xotirasida saqlanadi, bulutga sinxronlanmaydi. Brauzer ma'lumotlarini tozalash yoki ilovani olib tashlash ma'lumotlarni yo‘qotishi mumkin. Tez-tez JSON zaxira oling. Agar URL o‘zgarsa, boshqa xotira maydoni bo‘lishi mumkin. PWA beta varianti native ilovaning o‘rnini to‘liq bosa olmaydi.
