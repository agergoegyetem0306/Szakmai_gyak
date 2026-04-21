# Funkcionális követelmények

## 1. Bevezetés

A jelen fejezet célja a fejlesztés alatt álló gamifikált fitnessz mobilalkalmazás funkcionális követelményeinek meghatározása.  
A funkcionális követelmények azoknak a szolgáltatásoknak és viselkedéseknek az összességét írják le, amelyeket a rendszernek biztosítania kell a felhasználók számára.

Az alkalmazás célja az egészséges életmód támogatása játékosított elemek segítségével, különös tekintettel a rendszeres mozgásra, a tudatos táplálkozásra és a felhasználói motiváció fenntartására.

---

## 2. Felhasználókezelés és autentikáció

### 2.1 Regisztráció

- A rendszer lehetővé teszi új felhasználók számára, hogy email cím és jelszó megadásával regisztráljanak.
- A regisztráció során a rendszer ellenőrzi az adatok helyességét (érvényes email formátum, minimális jelszóhossz).
- Sikeres regisztráció esetén a felhasználói adatok eltárolásra kerülnek az adatbázisban.
- A jelszavak biztonsági okokból hash-elt formában kerülnek tárolásra.

### 2.2 Bejelentkezés

- A rendszer lehetőséget biztosít a felhasználók számára a regisztrált email cím és jelszó megadásával történő bejelentkezésre.
- Sikeres bejelentkezés esetén a backend egy egyedi azonosító tokent generál.
- A generált token a kliensoldalon eltárolásra kerül, és a további kérések hitelesítésére szolgál.

### 2.3 Kijelentkezés

- A rendszer lehetőséget biztosít a felhasználó számára a kijelentkezésre.
- Kijelentkezéskor a kliensoldalon eltárolt token törlésre kerül.
- Kijelentkezés után a felhasználó nem fér hozzá a védett funkciókhoz.

---

## 3. Jogosultságkezelés

- A rendszer megkülönbözteti a bejelentkezett és nem bejelentkezett felhasználókat.
- Bizonyos funkciók (pl. felhasználói adatok lekérdezése, aktivitások rögzítése) kizárólag hitelesített felhasználók számára érhetők el.
- A backend oldalon a token alapú hitelesítés biztosítja a védett végpontok elérésének szabályozását.

---

## 4. Felhasználói profil kezelés

- A rendszer képes a felhasználó alapvető adatainak (név, email) kezelésére.
- A felhasználó profiladatai kizárólag saját maga számára érhetők el.
- A profil adatok a későbbi funkciók (statisztikák, pontozás, kihívások) alapját képezik.

---

## 5. Aktivitás- és életmódkövetés 

- A rendszer lehetőséget biztosít a felhasználók számára napi fizikai aktivitások rögzítésére.
- A felhasználók rögzíthetik étkezéseiket és az elfogyasztott kalóriamennyiséget.
- A rendszer képes az adatok alapján összesítéseket és statisztikákat készíteni.
- A rögzített adatok hosszú távon is visszakereshetők.

---

## 6. Gamifikációs elemek

- A rendszer pontozási mechanizmust alkalmaz a felhasználói aktivitás ösztönzésére.
- A felhasználók különböző kihívásokat teljesíthetnek.
- A rendszer szintlépési lehetőséget biztosít a megszerzett pontok alapján.
- Jelvények és motivációs visszajelzések segítik a felhasználói elköteleződés fenntartását.

---

## 7. Közösségi funkciók 

- A rendszer lehetőséget biztosít felhasználói csoportok létrehozására az együttműködés és a motiváció növelése érdekében.
- A felhasználók meghívhatják ismerőseiket csoportokba, valamint csatlakozhatnak meglévő csoportokhoz.
- A csoporton belül a felhasználók alapvető módon nyomon követhetik egymás előrehaladását és aktivitását.
- A csoportfunkciók alapot biztosítanak a későbbi bővítésekhez, például rangsorok, közös kihívások és további gamifikációs elemek bevezetéséhez.

---

## 8. Összegzés

A fenti funkcionális követelmények meghatározzák az alkalmazás alapvető működését és fejlesztési irányát.  

