# Könyvtárinformatikai fogalomtár (DSpace feladathoz)

## 📚 Alapfogalmak

### Digitális repozitórium

Olyan rendszer, amely digitális dokumentumok (pl. szakdolgozatok,
cikkek) tárolására, kezelésére és szolgáltatására szolgál.\
**Kapcsolódás:** A feladatban a DSpace ilyen rendszer.

### DSpace

Nyílt forráskódú repozitórium szoftver intézményi dokumentumok
kezelésére.\
**Kapcsolódás:** A cél egy DSpace-be importálható csomag előállítása.

### Item (tétel)

A repozitóriumban egy egység (pl. egy szakdolgozat), amelyhez metaadatok
és fájlok tartoznak.\
**Kapcsolódás:** Az Excel egy sora egy item-et reprezentál.

------------------------------------------------------------------------

## 📑 Metaadatokkal kapcsolatos fogalmak

### Metaadat

Adat az adatról -- leíró információ egy dokumentumról (pl. szerző, cím,
év).\
**Kapcsolódás:** Az Excel oszlopai metaadatforrások.

### Dublin Core

Szabványos metaadat séma, amely alapmezőket definiál (pl. title,
creator, date).\
**Kapcsolódás:** A rendszer dublin_core.xml fájlt generál.

### Element

A Dublin Core mező fő neve (pl. title, date).\
**Kapcsolódás:** XML-ben element attribútumként jelenik meg.

### Qualifier

Az element finomítása (pl. contributor.author).\
**Kapcsolódás:** Pontosabb metaadat-leírást tesz lehetővé.

### dcvalue

A Dublin Core XML egy eleme, amely egy metaadatot reprezentál.\
**Kapcsolódás:** Egy Excel cella → egy vagy több dcvalue.

### Metaadat séma

A metaadatok struktúráját meghatározó rendszer (pl. Dublin Core).\
**Kapcsolódás:** A rendszer később bővíthető más sémákkal.

### Crosswalk (megfeleltetés)

Különböző metaadat-struktúrák közötti leképezés.\
**Kapcsolódás:** Excel oszlop → Dublin Core mező megfeleltetés.

------------------------------------------------------------------------

## 📂 Digitális objektumok és fájlkezelés

### Bitstream

A DSpace-ben egy tényleges digitális fájl (pl. PDF, kép).\
**Kapcsolódás:** Excelből vagy ZIP-ből kerül betöltésre.

### Bundle

Logikai csoport a fájlok számára egy itemen belül.\
**Példák:** ORIGINAL, TEXT, THUMBNAIL, PRESERVATION\
**Kapcsolódás:** Excel oszlopokhoz kötött.

### contents fájl

Szöveges fájl, amely felsorolja az itemhez tartozó fájlokat és azok
bundle-jét.\
**Kapcsolódás:** Automatikusan generálódik.

### Jogosultság (permissions)

Meghatározza, ki férhet hozzá egy fájlhoz.\
**Kapcsolódás:** Titkosított dokumentumoknál speciális beállítás.

------------------------------------------------------------------------

## 📦 Csomagolás és formátumok

### DSpace Simple Archive Format (SAF)

Import formátum, amely meghatározza: - mappastruktúrát - metaadat
fájlokat - contents fájlt\
**Kapcsolódás:** A kimenet egy ZIP SAF csomag.

### Könyvtárszerkezet

Az SAF csomag előírt struktúrája:

    item_000/
      dublin_core.xml
      contents
      file.pdf

**Kapcsolódás:** Automatikusan generálódik.

------------------------------------------------------------------------

## 🔐 Felhasználókezelés

### Hitelesítés (Authentication)

Felhasználó azonosítása (pl. LDAP, SAML).\
**Kapcsolódás:** Csak bejelentkezett felhasználók használhatják.

### Jogosultságkezelés (Authorization)

Meghatározza, ki milyen műveleteket végezhet.\
**Kapcsolódás:** Feltöltés és konfiguráció külön szabályozható.

------------------------------------------------------------------------

## 🔄 Adatfeldolgozás

### Import

Adatok betöltése a rendszerbe (Excel → SAF csomag).

### Validáció

Az adatok ellenőrzése (pl. hiányzó mezők, hibás útvonalak).

### Konfiguráció

Testreszabható beállítások (pl. crosswalk módosítása).

------------------------------------------------------------------------

## 🧠 Összefoglalás

A feladat fő területei: - metaadat-kezelés (Dublin Core) -
adattranszformáció (Excel → XML) - digitális objektumok kezelése
(bundle, bitstream) - archiválási csomag előállítása (SAF)
